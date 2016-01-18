package us.phyxsi.gameshelf.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.SimpleXMLConverter;
import us.phyxsi.gameshelf.R;
import us.phyxsi.gameshelf.data.api.bgg.BGGService;
import us.phyxsi.gameshelf.data.api.bgg.model.CollectionItems;
import us.phyxsi.gameshelf.data.api.bgg.model.User;
import us.phyxsi.gameshelf.data.prefs.BGGPrefs;
import us.phyxsi.gameshelf.ui.transitions.FabDialogMorphSetup;

public class BGGLogin extends Activity {

    boolean isDismissing = false;
    @Bind(R.id.container) ViewGroup container;
    @Bind(R.id.dialog_title) TextView title;
    @Bind(R.id.username_float_label) TextInputLayout usernameLabel;
    @Bind(R.id.username) AutoCompleteTextView username;
    @Bind(R.id.actions_container) FrameLayout actionsContainer;
    @Bind(R.id.login) Button login;
    @Bind(R.id.loading) ProgressBar loading;
    private BGGPrefs bggPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bgg_login);
        ButterKnife.bind(this);
        FabDialogMorphSetup.setupSharedEelementTransitions(this, container,
                getResources().getDimensionPixelSize(R.dimen.dialog_corners));

        loading.setVisibility(View.GONE);
        username.addTextChangedListener(loginFieldWatcher);
        bggPrefs = bggPrefs.get(this);
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

    public void doLogin(View view) {
        showLoading();
        getUser();
    }

    public void dismiss(View view) {
        isDismissing = true;
        setResult(Activity.RESULT_CANCELED);
        finishAfterTransition();
    }

    private TextWatcher loginFieldWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            login.setEnabled(isLoginValid());
        }
    };

    private boolean isLoginValid() {
        return username.length() > 0;
    }

    private void showLoading() {
        TransitionManager.beginDelayedTransition(container);
        title.setVisibility(View.GONE);
        usernameLabel.setVisibility(View.GONE);
        actionsContainer.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    private void showLogin() {
        TransitionManager.beginDelayedTransition(container);
        title.setVisibility(View.VISIBLE);
        usernameLabel.setVisibility(View.VISIBLE);
        actionsContainer.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    private void getUser() {
        BGGService bggApi = new RestAdapter.Builder()
                .setEndpoint(BGGService.ENDPOINT)
                .setConverter(new SimpleXMLConverter())
                .build()
                .create(BGGService.class);

        bggApi.getCollection(username.getText().toString(),
                new Callback<CollectionItems>() {
                    @Override
                    public void success(CollectionItems collectionItems, Response response) {
                        User user = new User(username.getText().toString());
                        bggPrefs.setLoggedInUser(user, collectionItems);

                        setResult(Activity.RESULT_OK);
                        finish();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(getClass().getCanonicalName(), error.getMessage(), error);

                        Toast.makeText(getApplicationContext(), "User not found",
                                Toast.LENGTH_LONG).show();

                        showLogin();
                    }
                });
    }
}
