/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.oculusvr.vrlib.VrActivity;

/**
 * The typical GVRF application will have a single Android {@link Activity},
 * which <em>must</em> descend from {@link GVRActivity}, not directly from
 * {@code Activity}.
 * 
 * {@code GVRActivity} creates and manages the internal classes which use sensor
 * data to manage a viewpoint, and thus present an appropriate stereoscopic view
 * of your scene graph. {@code GVRActivity} also gives GVRF a full-screen window
 * in landscape orientation with no title bar.
 */
public class GVRActivity extends VrActivity {

    public static final String TAG = "GVROculusActivity";

    private GVRViewManager mGVRViewManager = null;

    static {
        System.loadLibrary("gvrf");
    }

    public static native long nativeSetAppInterface(VrActivity act);

    static native void nativeSetCamera(long appPtr, long camera);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
         * Removes the title bar and the status bar.
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        super.onCreate(savedInstanceState);
        appPtr = nativeSetAppInterface(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGVRViewManager != null) {
            mGVRViewManager.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGVRViewManager != null) {
            mGVRViewManager.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGVRViewManager != null) {
            mGVRViewManager.onDestroy();
        }
    }

    /**
     * Links {@linkplain GVRScript a script} to the activity; sets the version;
     * sets the lens distortion compensation parameters; and sets the renderer
     * to be used. GVRF does provide a
     * {@linkplain GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
     * per-frame callback:} if you don't need the
     * {@link GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
     * onSurfaceCreated()} or
     * {@link GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)
     * onSurfaceChanged()} callbacks, you do not need to provide your own
     * {@link GVRSurfaceViewRenderer} implementation, and can use the simpler
     * {@link #setScript(GVRScript, String)} overload.
     * 
     * @param gvrScript
     *            An instance of {@link GVRScript} to handle callbacks on the GL
     *            thread.
     * @param distortionDataFileName
     *            Name of the XML file containing the device parameters. We
     *            currently only support the Galaxy Note 4 because that is the
     *            only shipping device with the proper GL extensions. When more
     *            devices support GVRF, we will publish new device files, along
     *            with app-level auto-detect guidelines. This approach will let
     *            you support new devices, using the same version of GVRF that
     *            you have already tested and approved.
     * 
     *            <p>
     *            The XML filename is relative to the application's
     *            {@code assets} directory, and can specify a file in a
     *            directory under the application's {@code assets} directory.
     * @param renderer
     *            A customized instance of {@link GVRSurfaceViewRenderer}. You
     *            only need to supply this if you need access to the
     *            {@link GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
     *            onSurfaceCreated()} or
     *            {@link GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)
     *            onSurfaceChanged()} callbacks.
     */
    public void setScript(GVRScript gvrScript, String distortionDataFileName,
            GVRSurfaceViewRenderer renderer) {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mGVRViewManager = new GVRViewManager(this, gvrScript,
                    distortionDataFileName, renderer);
        } else {
            throw new IllegalArgumentException(
                    "You can not set orientation to portrait for GVRF apps.");
        }
    }

    /**
     * Links {@linkplain GVRScript a script} to the activity; sets the version;
     * sets the lens distortion compensation parameters.
     * 
     * @param gvrScript
     *            An instance of {@link GVRScript} to handle callbacks on the GL
     *            thread.
     * @param distortionDataFileName
     *            Name of the XML file containing the device parameters. We
     *            currently only support the Galaxy Note 4 because that is the
     *            only shipping device with the proper GL extensions. When more
     *            devices support GVRF, we will publish new device files, along
     *            with app-level auto-detect guidelines. This approach will let
     *            you support new devices, using the same version of GVRF that
     *            you have already tested and approved.
     * 
     *            <p>
     *            The XML filename is relative to the application's
     *            {@code assets} directory, and can specify a file in a
     *            directory under the application's {@code assets} directory.
     */
    public void setScript(GVRScript gvrScript, String distortionDataFileName) {
        setScript(gvrScript, distortionDataFileName,
                (GVRSurfaceViewRenderer) null);
    }

    public void drawFrame() {
        mGVRViewManager.onDrawFrame();
    }

    public void oneTimeInit() {
        mGVRViewManager.onSurfaceCreated();
        Log.e(TAG, " oneTimeInit from native layer");
    }

    public void oneTimeShutDown() {
        Log.e(TAG, " oneTimeShutDown from native layer");
    }

    void onDrawEyeView(int eye, float fovDegrees) {
        mGVRViewManager.onDrawEyeView(eye, fovDegrees);
    }

    void setCamera(GVRCamera camera) {
        nativeSetCamera(appPtr, camera.getPtr());
    }
}
