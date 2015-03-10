package com.example.marcos.mobilepayment;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.util.Log;
import android.provider.Settings;
import android.app.ActionBar;
import android.app.Fragment;
import android.view.LayoutInflater;
import com.judopay.android.api.action.JudoPayments;


public class Main extends Activity {

    public static final int SERVER = JudoPayments.SANDBOX;
    public static final String JUDO_ID = "yourJudoId";
    public static final String API_TOKEN = "yourToken";
    public static final String API_SECRET = "yourSecret";
    public static final int ACTION_CARD_PAYMENT = 101;
    public static final String TAG = "My Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    public class Login extends Activity {

        Button btnEntrar;
        Button btnCadastrarNovo;
        private EditText editLogin;
        private EditText editSenha;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.login);

            // recuperando botões
            btnEntrar = (Button) findViewById(R.id.editEntrar);
            btnCadastrarNovo = (Button) findViewById(R.id.editCadastrarNovo);
            editLogin = (EditText) findViewById(R.id.editLogin);
            editSenha = (EditText) findViewById(R.id.editSenha);
            // implementar botão
            btnEntrar.setOnClickListener(new View.OnClickListener() {
                // invocando ação ao botão
                public void onClick(View v) {
                    startActivity(new Intent(getBaseContext(), Principal.class));
                }
            });
            // implementar botão
            btnCadastrarNovo.setOnClickListener(new View.OnClickListener() {
                // invocando ação ao botão
                public void onClick(View v) {
                    // esta ação irá fazer mudar de tela após ação do botão
                    startActivity(new Intent(getBaseContext(), CadastrarLogin.class));
                }
            });
        }
    }


    public void SimpleMakePayments(Context ctx,
                                   String amount,
                                   String judoID,
                                   String apiToken,
                                   String apiSecret,
                                   String paymentRef,
                                   String customerRef,
                                   String customerEmail,
                                   String customerPhone,
                                   boolean offerPinAccess,
                                   int serverID) {


        JudoPayments.init(ctx, apiToken, apiSecret, judoID, serverID);

        Intent i = new Intent(ctx, AddCardActivity.class);
        i.putExtra("JudoPay-amount", amount);
        i.putExtra("JudoPay-judoId", judoID);
        if (!offerPinAccess)
            i.putExtra("JudoPay-Mode", "basic");//this ensures they are not offered a chance to save their card details
        i.putExtra("JudoPay-yourPaymentReference", paymentRef);
        if (customerRef != null)//(if this parameter is not provided, customer ref will be assigned the device id)
            i.putExtra("JudoPay-yourConsumerReference", customerRef);

        //optional (For email or SMS receipts)
        if (customerEmail != null)
            i.putExtra("JudoPay-emailAddress", customerEmail);//your customer's email address (Email receipt)

        if (customerPhone != null)
            i.putExtra("JudoPay-mobileNumber", customerPhone);//your customer's mobile number (SMS receipt)

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    public static void ListTransactions(Context ctx){
        Intent i = new Intent (ctx, ListReceiptsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    public void makePayment(String amount, String paymentRef) {
// Initialize your key and secret before making any request to our api
        JudoSDKManager.setKeyAndSecret(getApplicationContext(), API_TOKEN, API_SECRET);

// Set environment to Sandbox
        JudoSDKManager.setSandboxMode(getApplicationContext());

        String customerRef = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Intent intent = JudoSDKManager.makeAPayment(getApplicationContext(), JUDO_ID, "GBP", amount, paymentRef, customerRef, null);
        startActivityForResult(intent, ACTION_CARD_PAYMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        // This is just an example of how we can handle the response
        switch (requestCode){
            case Globals.ACTION_CARD_PAYMENT:
                processPagamentoMovel(resultCode, data, getApplicationContext());
        }
    }

    public static void processPaymentResult(int resultCode, Intent data, Context context) {

        switch (resultCode) {
            case JudoSDKManager.JUDO_SUCCESS:
                Receipt receipt = data.getParcelableExtra(JudoSDKManager.JUDO_RECEIPT);
                Consumer consumer = receipt.getConsumer();
                CardToken cardToken = receipt.getCardToken();

                //Store card token, consumer token.

                Log.d(Globals.TAG, "Payment Successful: " + receipt.getReceiptId());

                Toast.makeText(context, "Payment Successful: " + receipt.getReceiptId(), Toast.LENGTH_SHORT).show();
                break;
            case JudoSDKManager.JUDO_CANCELLED:
                Log.d(Globals.TAG, "Payment Cancelled");
                Toast.makeText(context, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                break;
            case JudoSDKManager.JUDO_ERROR:
                Log.e(Globals.TAG, "Payment Error");
                Toast.makeText(context, "Payment Error", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

