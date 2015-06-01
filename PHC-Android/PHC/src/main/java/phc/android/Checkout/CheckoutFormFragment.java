package phc.android.Checkout;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import phc.android.R;

public class CheckoutFormFragment extends Fragment {
    /* Name for logs and fragment transaction code */
    public final static String TAG = "CheckoutFormFragment";

    /** Holds the result of the scan. */
    protected CharSequence mScanResult;
    protected Boolean mManualInput;

    /** Used to set listener to detect when the user rates their experience **/
    private RadioGroup mRadioGroup;

    /** Parent layout that holds all checkbox views. */
    private ViewGroup mLayout;

    /** Used to keep track IDs of checkboxes for services **/
    private ArrayList<CheckBox> checkBoxArray;

    /** Array of services applied but not received . */
    private ArrayList<String> mServices;

    /** Holds their input for experience rating 0-5 */
    private int mExperience;

    /** The textview that holds the comment. */
    private EditText mComment;

    /** Array of checked services still would like to receive . */
    private ArrayList<String> mServicesChecked;

    /** Used to set listener to detect when the user clicks submit **/
    private Button mCodeInputSubmitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_checkout_form, container, false);

        mComment = (EditText) view.findViewById(R.id.checkout_comment);
        mCodeInputSubmitButton = (Button) view.findViewById(R.id.button_submit);
        mCodeInputSubmitButton.setOnClickListener(new SubmitListener(getActivity()));

        if (savedInstanceState != null) {
            mScanResult = savedInstanceState.getCharSequence("scan_result");
            mManualInput = savedInstanceState.getBoolean("manual_input");
            mServices = savedInstanceState.getStringArrayList("services");

        } else {
            mScanResult =  getArguments().getCharSequence("scan_result");
            mManualInput = getArguments().getBoolean("manual_input");
            mServices = getArguments().getStringArrayList("services");
        }

        dynamicSetCheckboxes(view);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.checkout_radiogroup);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId){
                //Resource ids are not constants -> Can't use switch statements
                if (checkedId==R.id.checkout_radio_0){
                    mExperience = 0;
                }
                else if (checkedId==R.id.checkout_radio_1){
                    mExperience = 1;
                }
                else if (checkedId==R.id.checkout_radio_2){
                    mExperience = 2;
                }
                else if (checkedId==R.id.checkout_radio_3){
                    mExperience = 3;
                }
                else if (checkedId==R.id.checkout_radio_4){
                    mExperience = 4;
                }
                else if (checkedId==R.id.checkout_radio_5){
                    mExperience = 5;
                }
            }
        });

        return view;
    }


    /**
     * Adds each checked services they applied but didn't check in for to mServicesChecked(array)
     *
     */
    private void dynamicServiceCheck(){
        mServicesChecked = new ArrayList<String>();
            for (int i = 0 ;  i < checkBoxArray.size(); i++){
                // Loop through the checkBoxArray
                CheckBox mCheckBox = (CheckBox) checkBoxArray.get(i);
                if (mCheckBox.isChecked()){
                    // If the mCheckBox in the checkBoxArray is checked, add that corresponding service (mServices) to mServicesChecked
                    mServicesChecked.add(mServices.get(i));
                }
            }
        }

    /**
     * Dynamically populates layout with checkboxes for each service they applied but
     * not check in for.
     * Note: the current ID assignment method is rather hacky, but it's the only way that seems to
     * work. Android is not able to save and load dynamically created views even when their ID is
     * set, unless the views are created again with the same previous ID.
     */
    private void dynamicSetCheckboxes(View view){
        mLayout = (LinearLayout) view.findViewById(R.id.services_list);
        checkBoxArray = new ArrayList<CheckBox>();

        for(int i = 0; i < mServices.size(); i++){
            CheckBox cb = new CheckBox(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cb.setLayoutParams(params);
            cb.setId(i);
            cb.setText(mServices.get(i));
            mLayout.addView(cb);

            checkBoxArray.add(cb);
        }
    }


    /**
     * Used when the user submits their inputted code.
     */
    protected class SubmitListener implements View.OnClickListener{
        private Context mContext;

        public SubmitListener(Context context){
            mContext = context;
        }

        @Override
        public void onClick(View view){
            /**
             * Loads next fragment onto the current stack.
             */
            // Check which applied services they still want
            dynamicServiceCheck();

            Bundle args = new Bundle();
            args.putCharSequence("scan_result", mScanResult);
            args.putBoolean("manual_input", mManualInput);
            if(mComment == null ){
                args.putString("comments",  " ");
            }else{
                args.putString("comments",  mComment.getText().toString());
            }
            args.putInt("experience", mExperience);
            args.putStringArrayList("services", mServicesChecked);

            CheckoutConfirmationFragment confFrag = new CheckoutConfirmationFragment();
            confFrag.setArguments(args);
            displayNextFragment(confFrag, CheckoutConfirmationFragment.TAG);
        }
    }

    /**
     * Brings up another fragment when this fragment
     * is complete
     * @param nextFrag Fragment to display next
     * @param fragName String fragment names
     */

    protected void displayNextFragment(Fragment nextFrag, String fragName) {
        FragmentTransaction transaction =
                (getActivity()).getFragmentManager().beginTransaction();
        transaction.replace(R.id.checkout_activity_container, nextFrag, fragName);
        transaction.addToBackStack(fragName);
        transaction.commit();
    }


    /**
     * Used when the user wants to send an intent
     * to the scanner app.Confirmation
     */
    protected class ScanListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            startScan();
        }
    }

    /**
     * Starts the BarcodeScanner app.
     */
    protected void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    /**
     * Used to retrieve the result from the
     * BarcodeScanner app.
     *
     * @param reqCode int request code
     * @param resCode int result code
     * @param data Intent containing the result data
     */
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(reqCode, resCode, data);
        mScanResult = result.getContents();
        if (mScanResult == null) {
            showFailureToast();
        } else {
            confirmScan();
        }
    }

    /** Shows toast if the QR Scan was not successful. */
    protected void showFailureToast() {
        CharSequence message = getResources().getString(R.string.toast_scan_failure);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getActivity(), message, duration);
        toast.show();
    }

    /** Method that runs when a QR scan is successful. */
    protected void confirmScan() {
        CharSequence message = getResources().getString(R.string.toast_scan_success);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getActivity(), message, duration);
        toast.show();
        // TODO: Load success fragment: Onclick listener that wipes comments, experience, and services
        // TODO:
        // mComment.setText("");
        // reloadSuccessFragment();
    }
}
