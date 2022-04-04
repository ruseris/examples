/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package org.tensorflow.lite.examples.poseestimation

import android.R.attr
import org.tensorflow.lite.examples.poseestimation.data.BodyPart
import org.tensorflow.lite.examples.poseestimation.data.Person
import android.R.attr.angle
import android.graphics.*
import androidx.core.graphics.toColorInt
import kotlin.math.*


object VisualizationUtils {
    /** Radius of circle used to draw keypoints.  */
    private const val CIRCLE_RADIUS = 6f
    private const val CIRCLE_RADIUS_ADJUSTED = 4f

    /** Width of line used to connected two keypoints.  */
    private const val LINE_WIDTH = 4f
    private const val LINE_WIDTH_ADJUSTED = 2f

    /** The text size of the person id that will be displayed when the tracker is available.  */
    private const val PERSON_ID_TEXT_SIZE = 30f

    /** Distance from person id to the nose keypoint.  */
    private const val PERSON_ID_MARGIN = 6f

    /** Pair of keypoints to draw lines between.  */
    private val bodyJoints = listOf(
        Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
        Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
        Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
        Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
        Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
        Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    // TODO: connect with frontend
    private val objectToEstimate: String = "Left Hand"

    private val leftShoulder = Point();
    private val leftElbow = Point();
    private val leftWrist = Point();

    private val leftHip = Point();
    private var verticalAlignment = false;

    private val ANGLE_CHANGE_THRESHOLD = 2
    private var prevAngle = -999.0
    private val ANGLE_COUNTER_THRESHOLD = 5
    private var counter = 0
    private var numberCounter = 0
    private var displayVal = 0.0

    // Draw line and point indicate body pose
    fun drawBodyKeypoints(
        input: Bitmap,
        persons: List<Person>,
        isTrackerEnabled: Boolean = false
    ): Bitmap {
        val paintCircle = Paint().apply {
            strokeWidth = CIRCLE_RADIUS
            color = Color.RED
            style = Paint.Style.FILL
        }
        val paintLine = Paint().apply {
            strokeWidth = LINE_WIDTH
            //color = Color.RED
            color = "#E69289".toColorInt()
            style = Paint.Style.STROKE
        }

        val paintText = Paint().apply {
            textSize = PERSON_ID_TEXT_SIZE
            color = Color.BLUE
            textAlign = Paint.Align.LEFT
        }

        val paintTextAngle = Paint().apply {
            textSize = PERSON_ID_TEXT_SIZE
            //color = "#ADD8E6".toColorInt()
            color = "#4E78BC".toColorInt()
            textAlign = Paint.Align.LEFT
        }

        val paintTextCapturedAngle = Paint().apply {
            textSize = PERSON_ID_TEXT_SIZE
            //color = "#ADD8E6".toColorInt()
            color = "#4ECD3F".toColorInt()
            textAlign = Paint.Align.LEFT
        }

        val paintLineWhite = Paint().apply {
            strokeWidth = LINE_WIDTH_ADJUSTED
            color = Color.WHITE
            style = Paint.Style.STROKE
        }

        val paintLineCyan = Paint().apply {
            strokeWidth = LINE_WIDTH
            //color = Color.CYAN
            color = "#74AABE".toColorInt()
            style = Paint.Style.STROKE
        }

        val paintCircleWhite = Paint().apply {
            strokeWidth = CIRCLE_RADIUS_ADJUSTED
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val paintCircleCyan = Paint().apply {
            strokeWidth = CIRCLE_RADIUS_ADJUSTED
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val output = input.copy(Bitmap.Config.ARGB_8888, true)
        val originalSizeCanvas = Canvas(output)


        persons.forEach { person ->
            // draw person id if tracker is enable
            if (isTrackerEnabled) {
                person.boundingBox?.let {
                    val personIdX = max(0f, it.left)
                    val personIdY = max(0f, it.top)


                    originalSizeCanvas.drawText(
                        person.id.toString(),
                        personIdX,
                        personIdY - PERSON_ID_MARGIN,
                        paintText
                    )
                    originalSizeCanvas.drawRect(it, paintLine)
                }
            }
            println(objectToEstimate);

            /** Draws lines between points **/

            when (objectToEstimate) {
                "Full Body" -> {

                    bodyJoints.forEach {

                        val pointA = person.keyPoints[it.first.position].coordinate
                        val pointB = person.keyPoints[it.second.position].coordinate
                        originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLine)
                    }

                }
                "Left Hand" -> {
                    bodyJoints.forEach {
                        if((it.first == BodyPart.LEFT_SHOULDER) && (it.second == BodyPart.LEFT_ELBOW)
                                || (it.first == BodyPart.LEFT_ELBOW) && (it.second == BodyPart.LEFT_WRIST)) {
                            val pointA = person.keyPoints[it.first.position].coordinate
                            val pointB = person.keyPoints[it.second.position].coordinate
                            originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLineWhite)
                        }
                    }

                    // Extension: check if vertical
                    // https://stackoverflow.com/questions/23989355/checking-if-two-lines-are-nearly-parallel-gives-wrong-results


                    // Left Shoulder
                    val pointA = person.keyPoints[BodyPart.LEFT_SHOULDER.position].coordinate
                    // Left Hip
                    val pointB = person.keyPoints[BodyPart.LEFT_HIP.position].coordinate
                    // Left Elbow
                    val pointC = person.keyPoints[BodyPart.LEFT_ELBOW.position].coordinate

                    //originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLineCyan)
                    //originalSizeCanvas.drawLine(pointA.x, pointA.y, pointC.x, pointC.y, paintLine)

                    //dx1 - shoulder-hip
                    //dx2 - shoulder-elbow

                    val dx1 = pointB.x - pointA.x
                    val dy1 = pointB.y - pointA.y
                    val dx2 = pointC.x - pointA.x
                    val dy2 = pointC.y - pointA.y

                    val cosAngle = abs((dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)))

                    val threshold = cos(Math.toRadians(10.0))


//                    println("cosAngle: " + cosAngle.toDouble())
//                    println("cosAngle degrees: " + Math.toDegrees(cosAngle.toDouble()))
//                    println("threshold: " + abs(threshold))
//                    println("threshold degrees: " + abs(Math.toDegrees(threshold)))

                    //if(Math.toDegrees(cosAngle.toDouble()) > threshold) //threshold = cos(threshold angle)
                    verticalAlignment = cosAngle.toDouble() > threshold

                    /**
                     *
                    dx1 = la.X2 - la.X1
                    dy1 = la.Y2 - la.Y1
                    dx2 = lb.X2 - lb.X1
                    dy2 = lb.Y2 - lb.Y1
                    cosAngle = abs((dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)))
                     */


                    if (!verticalAlignment) {

                        originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLineCyan)

                        val pointA = person.keyPoints[BodyPart.LEFT_SHOULDER.position].coordinate
                        val pointB = person.keyPoints[BodyPart.LEFT_ELBOW.position].coordinate
                        originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLine)
                    }
                }
                "Left Leg" -> {

                    //TODO: implement

                }
                else -> {
                    print(
                        "objectToEstimate is not defined as expected. Check which object " +
                                "to draw."
                    )
                }
            }

//            bodyJoints.forEach {
//
//                val pointA = person.keyPoints[it.first.position].coordinate
//                val pointB = person.keyPoints[it.second.position].coordinate
//                originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLine)
//            }

            /** Draws points on image **/

            person.keyPoints.forEach { point ->

                when (objectToEstimate) {
                    "Full Body" -> {
                        originalSizeCanvas.drawCircle(
                            point.coordinate.x,
                            point.coordinate.y,
                            CIRCLE_RADIUS,
                            paintCircleWhite
                        )
                    }

                    "Left Hand" -> {

                        // Draw circles only for 3 points
                        if ((point.bodyPart == BodyPart.LEFT_SHOULDER)
                            || (point.bodyPart == BodyPart.LEFT_ELBOW)
                            || (point.bodyPart == BodyPart.LEFT_WRIST)) {

                            if (point.bodyPart == BodyPart.LEFT_SHOULDER) {
                                leftShoulder.x = point.coordinate.x.toInt();
                                leftShoulder.y = point.coordinate.y.toInt();
                            } else if (point.bodyPart == BodyPart.LEFT_ELBOW) {
                                leftElbow.x = point.coordinate.x.toInt();
                                leftElbow.y = point.coordinate.y.toInt();
                            } else {
                                leftWrist.x = point.coordinate.x.toInt();
                                leftWrist.y = point.coordinate.y.toInt();
                            }


                            originalSizeCanvas.drawCircle(
                                point.coordinate.x,
                                point.coordinate.y,
                                CIRCLE_RADIUS,
                                paintCircleWhite
                            )
                        }

                        // Extension: draw hip point

                        if (!verticalAlignment) {

                            if (point.bodyPart == BodyPart.LEFT_HIP) {
                                originalSizeCanvas.drawCircle(
                                    point.coordinate.x,
                                    point.coordinate.y,
                                    CIRCLE_RADIUS,
                                    paintCircleCyan
                                )
                            }

                        }


                    }

                    "Left Leg" -> {

                        //TODO: implement

                    }
                    else -> {
                        print("objectToEstimate is not defined as expected. Check which object " +
                                "to draw.")
                    }
                }




            }




            // Elbow flextion
            // TODO: degrees are not great, fix them.

//            var leftShoulder = person.keyPoints.get(5).coordinate
//            var leftElbow = person.keyPoints.get(7).coordinate
//            var leftWrist = person.keyPoints.get(9).coordinate
//
//            var leftElbowFlexion = ((Math.atan2((leftWrist.y - leftElbow.y).toDouble(),
//                    (leftWrist.x - leftElbow.x).toDouble())
//                    - Math.atan2((leftShoulder.y - leftElbow.y).toDouble(),
//                    (leftShoulder.x - leftElbow.x).toDouble())) * (180 / Math.PI))
//
//            val leftElbowTextX = max(0f, leftElbow.x + 20)
//            val leftElbowTextY = max(0f, leftElbow.y + 20)
//
//            originalSizeCanvas.drawText(
//                leftElbowFlexion.toString(),
//                leftElbowTextX,
//                leftElbowTextY,
//                paintText
//            )

            /** Draws left elbow angle **/

            //var result = (Math.atan2(leftWrist.y.toDouble() - leftElbow.y, leftWrist.x.toDouble()  - leftElbow.x) - Math.atan2(
            //    leftShoulder.y.toDouble()  - leftElbow.y, leftShoulder.x.toDouble()  - leftElbow.x)) * (180 / Math.PI);



            var result = abs(abs((Math.atan2(leftWrist.y.toDouble() - leftElbow.y, leftWrist.x.toDouble()  - leftElbow.x) - Math.atan2(
                leftShoulder.y.toDouble()  - leftElbow.y, leftShoulder.x.toDouble()  - leftElbow.x)) * (180 / Math.PI)) - 180);

            println("abs prev angle - Angle: " + abs(prevAngle - result))
            //println("")

            //println("Prev angle: " + prevAngle)



            var difference = abs(prevAngle - result)

            // If small change, do not redraw
            if (( difference < ANGLE_CHANGE_THRESHOLD) && (difference > 0)) {
                counter++
                numberCounter++
                println("angel count:" + counter)

                originalSizeCanvas.drawText(
                    String.format("%.0f", prevAngle),
                    (leftElbow.x - 10).toFloat(),
                    (leftElbow.y + 50).toFloat(),
                    paintTextAngle
                )

                if (counter > ANGLE_COUNTER_THRESHOLD){

                    if (numberCounter > ANGLE_COUNTER_THRESHOLD) {
                        displayVal = prevAngle
                        numberCounter = 0
                    }

                    originalSizeCanvas.drawText(
                        String.format("%.0f", displayVal),
                        (leftElbow.x - 10).toFloat(),
                        (leftElbow.y + 100).toFloat(),
                        paintTextCapturedAngle
                    )
                }

                prevAngle = result
                return output
            }

            prevAngle = result

            counter = 0
            numberCounter = 0
            originalSizeCanvas.drawText(
                String.format("%.0f", result),
                (leftElbow.x - 10).toFloat(),
                (leftElbow.y + 50).toFloat(),
                paintTextAngle
            )

        }


        return output
    }
}
