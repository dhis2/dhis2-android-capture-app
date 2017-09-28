package com.dhis2.usescases.login;


import com.dhis2.databinding.ActivityLoginBinding;
import com.dhis2.usescases.general.AbstractActivityContracts;

public class LoginContracts {

    interface View extends AbstractActivityContracts.View {
        ActivityLoginBinding getBinding();
    }

    interface Presenter {
        void onTextChanged(CharSequence s, int start, int before, int count);
        void validateCredentials();
    }

    interface Interactor {

    }

    interface Router {

    }

}