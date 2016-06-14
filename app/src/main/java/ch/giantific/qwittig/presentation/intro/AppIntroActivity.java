package ch.giantific.qwittig.presentation.intro;

import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import ch.giantific.qwittig.R;

public class AppIntroActivity extends IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setButtonBackFunction(BUTTON_BACK_FUNCTION_BACK);
        setButtonCtaVisible(true);

        addSlide(new SimpleSlide.Builder()
                .title("some title")
                .description("some description")
                .image(R.drawable.ic_shopping_cart_black_144dp)
                .background(R.color.indigo_500)
                .backgroundDark(R.color.indigo_700)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("some other title")
                .description("some other description")
                .image(R.drawable.ic_attach_money_black_144dp)
                .background(R.color.red_500)
                .backgroundDark(R.color.red_700)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("some other title again")
                .description("some other description agin")
                .image(R.drawable.ic_account_box_black_144dp)
                .background(R.color.green_500)
                .backgroundDark(R.color.green_700)
                .build());
    }
}
