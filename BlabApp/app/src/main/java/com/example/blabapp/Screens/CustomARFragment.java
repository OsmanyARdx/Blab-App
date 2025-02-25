package com.example.blabapp.Screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.fragment.app.Fragment;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public class CustomARFragment extends ArFragment {


    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = new Config(session);

        // ✅ Ensure ARCore is set for Augmented Faces (Front Camera Only)
        config.setAugmentedFaceMode(Config.AugmentedFaceMode.MESH3D);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);

        // ✅ Apply configuration before setting up ArSceneView
        session.configure(config);

        if (this.getArSceneView() != null) {
            this.getArSceneView().setupSession(session);
        } else {
            throw new RuntimeException("ArSceneView is NULL! Check if fragment is attached correctly.");
        }

        return config;
    }






    @Override
    protected Set<Session.Feature> getSessionFeatures() {
        return EnumSet.of(Session.Feature.FRONT_CAMERA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable @androidx.annotation.Nullable ViewGroup container, @Nullable @androidx.annotation.Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);

        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);

        return frameLayout;
    }
}
