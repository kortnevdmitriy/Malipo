package ai.kortnevdmitriy.malipo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import io.fabric.sdk.android.Fabric;
import mp.MpUtils;
import mp.PaymentRequest;
import mp.PaymentResponse;

public class MainActivity extends AppCompatActivity {
    // Fortumo related glue-code
    private static final int REQUEST_CODE = 1234; // Can be anything
    private Button btnBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnBuy = (Button) findViewById(R.id.btnBuy); // find button by id.
        // Set an onClickListener on the button to build a payment request.
        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaymentRequest.PaymentRequestBuilder builder = new PaymentRequest.PaymentRequestBuilder();
                builder.setService(Constants.SERVICE_ID, Constants.APP_SECRET);
                builder.setDisplayString("Malipo");
                builder.setProductName("gamerId_level1");  // non-consumable purchases are restored using this value
                builder.setType(MpUtils.PRODUCT_TYPE_CONSUMABLE);              // non-consumable items can be later restored
                builder.setIcon(R.mipmap.ic_launcher);
                PaymentRequest pr = builder.build();
                makePayment(pr);
            }
        });

    }

    // Make payment method takes in the payment request as a parameter.
    protected final void makePayment(PaymentRequest payment) {
        startActivityForResult(payment.toIntent(this), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (data == null) {
                return;
            }

            // OK
            if (resultCode == RESULT_OK) {
                PaymentResponse response = new PaymentResponse(data);

                switch (response.getBillingStatus()) {
                    case MpUtils.MESSAGE_STATUS_BILLED:
                        // ...
                        break;
                    case MpUtils.MESSAGE_STATUS_FAILED:
                        // ...
                        break;
                    case MpUtils.MESSAGE_STATUS_PENDING:
                        // ...
                        break;
                }
                // Cancel
            } else {
                // ..
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /*----------------------------------------------------------------------------------------------
    *  ACCEPTING CREDIT CARD PAYMENT USING STRIPE API
    -----------------------------------------------------------------------------------------------*/
    public void submitCard(View view) {
        // TODO: replace with your own test key
        final String publishableApiKey = BuildConfig.DEBUG ?
                "pk_test_6pRNASCoBOKtIshFeQd4XMUh" :
                getString(R.string.com_stripe_publishable_key);

        TextView cardNumberField = (TextView) findViewById(R.id.cardNumber);
        TextView monthField = (TextView) findViewById(R.id.month);
        TextView yearField = (TextView) findViewById(R.id.year);
        TextView cvcField = (TextView) findViewById(R.id.cvc);

        Card card = new Card(cardNumberField.getText().toString(),
                Integer.valueOf(monthField.getText().toString()),
                Integer.valueOf(yearField.getText().toString()),
                cvcField.getText().toString());

        Stripe stripe = new Stripe();
        stripe.createToken(card, publishableApiKey, new TokenCallback() {
            public void onSuccess(Token token) {
                // TODO: Send Token information to your backend to initiate a charge
                Toast.makeText(
                        getApplicationContext(),
                        "Token created: " + token.getId(),
                        Toast.LENGTH_LONG).show();
            }

            public void onError(Exception error) {
                Log.d("Stripe", error.getLocalizedMessage());
            }
        });
    /*---------------------------------------------------------------------------------------------*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
