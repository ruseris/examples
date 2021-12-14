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
import kotlin.math.atan2
import kotlin.math.max
import android.R.attr.angle
import android.graphics.*


object VisualizationUtils {
    /** Radius of circle used to draw keypoints.  */
    private const val CIRCLE_RADIUS = 6f

    /** Width of line used to connected two keypoints.  */
    private const val LINE_WIDTH = 4f

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
            color = Color.RED
            style = Paint.Style.STROKE
        }

        val paintText = Paint().apply {
            textSize = PERSON_ID_TEXT_SIZE
            color = Color.BLUE
            textAlign = Paint.Align.LEFT
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
                            originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLine)
                        }
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
                            paintCircle
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
                                paintCircle
                            )
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



            var result = (Math.atan2(leftWrist.y.toDouble() - leftElbow.y, leftWrist.x.toDouble()  - leftElbow.x) - Math.atan2(
                leftShoulder.y.toDouble()  - leftElbow.y, leftShoulder.x.toDouble()  - leftElbow.x)) * (180 / Math.PI);

            originalSizeCanvas.drawText(
                    result.toString(),
                (leftElbow.x - 20).toFloat(),
                    leftElbow.y.toFloat(),
                    paintText
                )


            println(person)
            println(person.keyPoints.get(5).coordinate.x)
            println(person.keyPoints.get(5).coordinate.y)

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



        }


        return output
    }
}
