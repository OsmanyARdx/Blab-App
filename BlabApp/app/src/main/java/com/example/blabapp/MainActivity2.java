package com.example.blabapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.blabapp.Screens.CustomARFragment;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnsupportedConfigurationException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.AugmentedFaceNode;

import java.util.Collection;

public class MainActivity2 extends AppCompatActivity {

    private ModelRenderable modelRenderable;
    private Texture texture;
    private boolean isAdded = false;
    private CustomARFragment customARFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_blank);

        getSupportFragmentManager().executePendingTransactions();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.arFragment);

        if (fragment instanceof CustomARFragment) {
            customARFragment = (CustomARFragment) fragment;
        } else {
            Log.e("MainActivity2", "CustomARFragment not found!");
            return; // Stop execution if fragment isn't found
        }

        // ✅ Wait until the fragment's view is ready before accessing ArSceneView
        customARFragment.getViewLifecycleOwnerLiveData().observe(this, viewLifecycleOwner -> {
            if (viewLifecycleOwner != null) {
                ArSceneView sceneView = customARFragment.getArSceneView();
                if (sceneView == null) {
                    Log.e("MainActivity2", "ArSceneView is NULL even after waiting!");
                    return;
                }

                // Set ARCore camera stream priority
                sceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);

                // Add face tracking listener
                sceneView.getScene().addOnUpdateListener(frameTime -> trackFaces(sceneView));
            }
        });

        // Load the 3D Model
        ModelRenderable.builder()
                .setSource(this, R.raw.fox_face)
                .build()
                .thenAccept(renderable -> {
                    modelRenderable = renderable;
                    modelRenderable.setShadowCaster(false);
                    modelRenderable.setShadowReceiver(false);
                    Log.d("MainActivity2", "ModelRenderable loaded successfully");
                })
                .exceptionally(throwable -> {
                    Log.e("MainActivity2", "Failed to load ModelRenderable", throwable);
                    return null;
                });




        // Load the Texture
        Texture.builder()
                .setSource(this, R.drawable.m)
                .build()
                .thenAccept(texture -> {
                    this.texture = texture;
                    Log.d("MainActivity2", "Texture loaded successfully");
                })
                .exceptionally(throwable -> {
                    Log.e("MainActivity2", "Failed to load texture", throwable);
                    return null;
                });
    }

    private void trackFaces(ArSceneView sceneView) {
        if (modelRenderable == null || texture == null) {
            Log.w("MainActivity2", "ModelRenderable or Texture is not loaded yet.");
            return;
        }

        Frame frame = sceneView.getArFrame();
        if (frame == null) {
            Log.w("MainActivity2", "AR Frame is null.");
            return;
        }

        Collection<AugmentedFace> augmentedFaces = frame.getUpdatedTrackables(AugmentedFace.class);
        if (augmentedFaces.isEmpty()) {
            Log.w("MainActivity2", "No faces detected. Ensure the front camera is used and the environment is well-lit.");
        }

        for (AugmentedFace augmentedFace : augmentedFaces) {
            if (augmentedFace.getTrackingState() == TrackingState.TRACKING) {
                Log.d("MainActivity2", "Face detected! Applying filter...");

                if (!isAdded) {
                    AugmentedFaceNode augmentedFaceNode = new AugmentedFaceNode(augmentedFace);
                    augmentedFaceNode.setParent(sceneView.getScene());

                    // ✅ Attach face filter model and texture

                    augmentedFaceNode.setFaceMeshTexture(texture);

                    isAdded = true;
                    Log.d("MainActivity2", "Face filter applied successfully!");
                }
            } else {
                Log.w("MainActivity2", "Face detected but not fully tracked yet.");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            Session session = new Session(this);
            Config config = new Config(session);

            try {
                // ✅ Check if Augmented Faces is supported before enabling it
                config.setAugmentedFaceMode(Config.AugmentedFaceMode.MESH3D);
            } catch (UnsupportedOperationException e) {
                Log.e("MainActivity2", "Augmented Faces is NOT supported on this device.");
                return;
            }

            config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            session.configure(config);

            if (customARFragment != null && customARFragment.getArSceneView() != null) {
                customARFragment.getArSceneView().setupSession(session);
            } else {
                Log.e("MainActivity2", "ArSceneView is NULL in onResume! Fragment may not be fully initialized.");
            }

        } catch (UnavailableArcoreNotInstalledException |
                 UnavailableApkTooOldException |
                 UnavailableSdkTooOldException |
                 UnavailableDeviceNotCompatibleException e) {
            Log.e("MainActivity2", "Failed to initialize ARCore session", e);
        } catch (UnsupportedConfigurationException e) {
            Log.e("MainActivity2", "ARCore does not support this configuration", e);
        }
    }
}
