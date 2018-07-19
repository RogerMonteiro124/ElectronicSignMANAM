package roger.monteiro.app.electronicsignmanam;


import android.app.Activity;
import android.os.Bundle;

import roger.monteiro.app.electronicsignmanam.LayoutPrincipalAssinatura;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(new LayoutPrincipalAssinatura(this));
    }
}