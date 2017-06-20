package vn.edu.sunny.myapplication;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class TestKnobFragment extends Fragment
{
    private RotorKnob knob;

    public TestKnobFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test_knob, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        knob=(RotorKnob)view.findViewById(R.id.rotorKnob);
        knob.setMinVal(0);
        knob.setMaxVal(100);
        knob.setOnDataChange(new RotorKnob.onDataChangeEvent() {
            @Override
            public void onDataChange(float progress) {
                Log.d("huy", "ondata change "+(int)progress);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        knob.release();
    }
}
