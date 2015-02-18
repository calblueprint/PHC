package phc.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

/**
 * SuccessFragment is launched on successful submission of a client's form data,
 * and allows the user to go back to activity_register another client.
 */
public class SuccessFragment extends RegistrationFragment {

    // Different flows this success fragment can be in
    public static enum SuccessType {REGISTER_SUCCESS, SERVICE_SUCCESS, CHECKOUT_SUCCESS};
    // Submit button
    private Button mRepeatActionButton;
    // The flow this success fragment is in
    private SuccessType mCurrentSuccessType;

    private static final String TAG = "SUCCESS_FRAGMENT";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_success, container, false);
        setSuccessName(view);
        setOnRegisterAnotherClickListener(view);
        return view;
    }

    @Override
    public void onResume() {
        switch (mCurrentSuccessType) {
            case REGISTER_SUCCESS:
                LinearLayout sidebarList = (LinearLayout) getActivity().findViewById(R.id.sidebar_list);
                for (int i = 0; i < sidebarList.getChildCount(); i++) {
                    View v = sidebarList.getChildAt(i);
                    Object vTag = v.getTag();
                    if ((vTag != null) && (vTag.equals(getResources().getText(R.string.sidebar_confirm)))) {
                        TextView tv = (TextView) v;
                        tv.setTypeface(null, Typeface.BOLD);
                    } else if (v instanceof TextView) {
                        TextView tv = (TextView) v;
                        tv.setTypeface(null, Typeface.NORMAL);
                    }
                }
                break;
            case SERVICE_SUCCESS:
                break;
            case CHECKOUT_SUCCESS:
                break;
            default:
                Log.e(TAG, "Did not set the success type using the setType() method");
                break;

        }

        super.onResume();
    }

    /**
     * Should be called from the previous fragment to modify the success fragment based on what flow it is in
     */
    public void setType(SuccessType successType) {
        mCurrentSuccessType = successType;
    }

    /**
     * Creates an OnClickListener for the "Register Another Client" button,
     * which calls a new instance of RegisterActivity.
     */
    protected void setOnRegisterAnotherClickListener(View view) {
        mRepeatActionButton = (Button) view.findViewById(R.id.button_register_another);
        mRepeatActionButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), RegisterActivity.class);
                        HashMap<String, String> services = ((RegisterActivity)getActivity())
                                .getServices();
                        intent.putExtra("services_hashmap", services);
                        String eventId = ((RegisterActivity) getActivity()).getmEventId();
                        intent.putExtra("event_id", eventId);
                        getActivity().finish();
                        getActivity().startActivity(intent);
                    }
        });
    }

    /**
     * Grabs the person's name from SharedPreferences, and adds it after the Success text
     */
    protected void setSuccessName(View view) {
        TextView mSuccessText = (TextView) view.findViewById(R.id.text_success);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SharedPreferenceEditorListener.USER_PREFS_NAME, Context.MODE_PRIVATE);
        String firstName = sharedPref.getString("first_name", "");
        String lastName = sharedPref.getString("last_name", "");
        mSuccessText.setText("Success! " + firstName + " " + lastName + " is now registered.");
    }
}
