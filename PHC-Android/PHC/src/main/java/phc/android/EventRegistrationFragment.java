package phc.android;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * EventRegistrationFragment is the event registration form for all clients
 * and includes fields that might have changed since the last event.
 */
public class EventRegistrationFragment extends Fragment{
    /* Continue button */
    Button mSubmitButton;
    /**
     * On creation of the fragment, sets content for spinners and an onClickListener
     * for the continue button.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout ll;
        String[] services;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_registration, container, false);

        // Grab list of all services offered for the current event from Salesforce DB
        // Dynamically populate linear layout with checkboxes for each service
        Resources res = getResources();

        services = res.getStringArray(R.array.medical_services_array);
        ll = (LinearLayout) view.findViewById(R.id.medical_services_list);
        for (String service : services) {
            CheckBox cb = new CheckBox(getActivity());
            cb.setLayoutParams(new LinearLayout.LayoutParams(
                    R.dimen.input_text_width,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            cb.setText(service);
            ll.addView(cb);
        }

        services = res.getStringArray(R.array.support_services_array);
        ll = (LinearLayout) view.findViewById(R.id.support_services_list);
        for (String service : services) {
            CheckBox cb = new CheckBox(getActivity());
            cb.setLayoutParams(new LinearLayout.LayoutParams(
                    R.dimen.input_text_width,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            cb.setText(service);
            ll.addView(cb);
        }

        mSubmitButton = (Button) view.findViewById(R.id.button_submit);
        mSubmitButton.setOnClickListener(new OnSubmitClickListener(getActivity()));

        return view;
    }

    @Override
    public void onResume() {
        LinearLayout sidebarList = (LinearLayout) getActivity().findViewById(R.id.sidebar_list);
        for (int i = 0; i < sidebarList.getChildCount(); i++) {
            View v = sidebarList.getChildAt(i);
            Object vTag = v.getTag();
            if ((vTag != null) && (vTag.equals(getResources().getText(R.string.sidebar_event_info)))) {
                TextView tv = (TextView) v;
                tv.setTypeface(null, Typeface.BOLD);
            } else if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTypeface(null, Typeface.NORMAL);
            }
        }
        super.onResume();
    }
}
