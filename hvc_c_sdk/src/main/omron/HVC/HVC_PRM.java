/*
 * Copyright (C) 2014-2015 OMRON Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package omron.HVC;

/**
 * HVC parameters
 */
public class HVC_PRM
{
    /**
     * Pose enum
     */
    public enum HVC_FACE_POSE {
        HVC_FACE_POSE_FRONT(0),
        HVC_FACE_POSE_HALF_PROFIL(1),
        HVC_FACE_POSE_PROFILE(2);

        private int value;
        private HVC_FACE_POSE(int n) {
    		this.value = n;
    	}
    	public int getValue() {
    		return this.value;
    	}
    	public void setValue(int n) {
    		this.value = n;
    	}
    }
    /**
     * Angle enum
     */
    public enum HVC_FACE_ANGLE {
        HVC_FACE_ANGLE_15(0),
        HVC_FACE_ANGLE_45(1);

        private int value;
        private HVC_FACE_ANGLE(int n) {
    		this.value = n;
    	}
    	public int getValue() {
    		return this.value;
    	}
    	public void setValue(int n) {
    		this.value = n;
    	}
    }

    /**
     * Camera angle enum
     */
    public enum HVC_CAMERA_ANGLE {
        HVC_CAMERA_ANGLE_0(0),
        HVC_CAMERA_ANGLE_90(1),
        HVC_CAMERA_ANGLE_180(2),
        HVC_CAMERA_ANGLE_270(3);

        private int value;
        private HVC_CAMERA_ANGLE(int n) {
    		this.value = n;
    	}
    	public int getValue() {
    		return this.value;
    	}
    	public void setValue(int n) {
    		this.value = n;
    	}
    }

    /**
     * Detection parameters
     */
    public class DetectionParam
    {
        /**
         * Minimum detection size
         */
        public int MinSize;
        /**
         * Maximum detection size
         */
        public int MaxSize;
        /**
         * Degree of confidence
         */
        public int Threshold;

        /**
         * Constructor<br>
         * [Description]<br>
         * none<br>
         * [Notes]<br>
         */
        public DetectionParam()
        {
            MinSize = 40;
            MaxSize = 480;
            Threshold = 500;
        }
    }

    /**
     * Face Detection parameters
     */
    public class FaceParam extends DetectionParam
    {
        /**
         * Facial pose
         */
        public HVC_FACE_POSE pose;
        /**
         * Roll angle
         */
        public HVC_FACE_ANGLE angle;

        /**
         * Constructor<br>
         * [Description]<br>
         * none<br>
         * [Notes]<br>
         */
        public FaceParam()
        {
            pose = HVC_FACE_POSE.HVC_FACE_POSE_FRONT;
            angle = HVC_FACE_ANGLE.HVC_FACE_ANGLE_15;
        }
    }

    /**
     * Camera angle
     */
    public HVC_CAMERA_ANGLE cameraAngle;
    /**
     * Human Body Detection parameters
     */
    public DetectionParam body;
    /**
     * Hand Detection parameters
     */
    public DetectionParam hand;
    /**
     * Face Detection parameters
     */
    public FaceParam face;

    /**
     * Constructor<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     */
    public HVC_PRM()
    {
        cameraAngle = HVC_CAMERA_ANGLE.HVC_CAMERA_ANGLE_0;

        body = new DetectionParam();
        hand = new DetectionParam();
        face = new FaceParam();
    }
}
