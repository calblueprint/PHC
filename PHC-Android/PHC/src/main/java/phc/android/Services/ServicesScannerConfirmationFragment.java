package phc.android.Services;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import phc.android.Helpers.SearchResult;
import phc.android.R;
import phc.android.SharedFragments.ScannerConfirmationFragment;
import phc.android.SharedFragments.SuccessFragment;

/**
 * Created by Byronium on 3/9/15.
 */
public class ServicesScannerConfirmationFragment extends ScannerConfirmationFragment{

    /* Tag for logs and fragment code */
    public final static String TAG = "SERVICES_SCANNER_CONF";

    /* Holds the response from requestSearchByCode (true if qr code matches). */
    private boolean mRegistrationFound = false;

    /**
     * Looks up the qr code to make sure it matches one of the registrations. If found,
     * updates the service field of that registration object.
     */
    @Override
    protected void confirm() {

        sRequestManager.requestSearchByCode(mScanResult,
                mAuthToken,
                mUserId,
                new SearchByCodeResponseListener(),
                new SearchByCodeErrorListener());

        if (mRegistrationFound) {

            sRequestManager.requestUpdateService(mScanResult,
                    ((ServicesActivity)getActivity()).getServiceSelected(),
                    mAuthToken,
                    mUserId,
                    new UpdateServiceResponseListener(),
                    new UpdateServiceErrorListener());

        } else {
            notFoundDialogue();
        }
    }

    /**
     * Loads SuccessFragment with Services type.
     */
    private void loadServicesSuccess(){
        FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
        SuccessFragment successFragment = new SuccessFragment();
        successFragment.setType(SuccessFragment.SuccessType.SERVICE_SUCCESS);
        transaction.replace(R.id.service_fragment_container, successFragment);
        transaction.commit();
    }

    /**
     * Creates an alert dialogue to tell the user that the qr code is not found,
     * along with a button to try another.
     */
    public void notFoundDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                retry();
            }
        });
        builder.setTitle("QR Code not found.");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class SearchByCodeResponseListener implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                mRegistrationFound = jsonObject.getBoolean("present");
            } catch(JSONException e) {
                Log.e(TAG, "Error parsing JSON");
                Log.e(TAG, e.toString());
            }
        }
    }

    private class SearchByCodeErrorListener implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (volleyError.getLocalizedMessage() != null) {
                Log.e(TAG, volleyError.toString());
            }

            Toast toast = Toast.makeText(getActivity(), "Error looking up the QR Code",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class UpdateServiceResponseListener implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                String message = jsonObject.getString("message");
                // makes a toast if receive an unexpected service status message
                // (i.e. client is a drop-in or client has already used the service)
                if (message != ""){
                    Context c = getActivity();
                    Toast toast = Toast.makeText(c, message, Toast.LENGTH_SHORT);
                    toast.show();
                }

                loadServicesSuccess();

            } catch(JSONException e) {
                Log.e(TAG, "Error parsing JSON");
                Log.e(TAG, e.toString());
            }
        }
    }

    private class UpdateServiceErrorListener implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (volleyError.getLocalizedMessage() != null) {
                Log.e(TAG, volleyError.toString());
            }

            Toast toast = Toast.makeText(getActivity(), "Error updating service.",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
