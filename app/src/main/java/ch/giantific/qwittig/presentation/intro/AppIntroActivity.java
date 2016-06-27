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
                .title("Scan")
                .description("Take a picture of your receipt and Qwittig will digitalize it for you.")
                .image(R.drawable.ic_shopping_cart_black_144dp)
                .background(R.color.red)
                .backgroundDark(R.color.red_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Split")
                .description("Choose which items belong to whom and let Qwittig do the math.")
                .image(R.drawable.ic_account_box_black_144dp)
                .background(R.color.blue)
                .backgroundDark(R.color.blue_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Settle")
                .description("Lean back, Qwittig will remind everybody to pay their debts.")
                .image(R.drawable.ic_attach_money_black_144dp)
                .background(R.color.green)
                .backgroundDark(R.color.green_dark)
                .build());
    }
}
