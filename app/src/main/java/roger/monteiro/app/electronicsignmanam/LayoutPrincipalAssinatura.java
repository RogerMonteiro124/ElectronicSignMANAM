package roger.monteiro.app.electronicsignmanam;

/**
 * Created by roger on 18/07/18.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LayoutPrincipalAssinatura extends LinearLayout implements OnClickListener {

    LinearLayout buttonsLayout;
    SignatureView signatureView;

    public LayoutPrincipalAssinatura(Context context) {
        super(context);

        this.setOrientation(LinearLayout.VERTICAL);

        this.buttonsLayout = this.buttonsLayout();
        this.signatureView = new SignatureView(context);

        // Adicionando a view da assinatura
        this.addView(this.buttonsLayout);
        this.addView(signatureView);

    }

    private LinearLayout buttonsLayout() {

        // Criando a interface para usuário UI
        LinearLayout linearLayout = new LinearLayout(this.getContext());

        Button saveBtn = new Button(this.getContext());
        Button clearBtn = new Button(this.getContext());
        TextView nomeAss = new TextView(this.getContext());

        // Setando a orientacao
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setBackgroundColor(Color.CYAN);

        // Setando text, tags e Hint
        nomeAss.setText("Aqui vem todo o termo, mas com os campos editaves como\n: nome,\n matricula,\n funcao,\n setor,\n data de entrega,\n");
        nomeAss.setTag("main");
        nomeAss.setOnClickListener(this);

        saveBtn.setText("Salvar");
        saveBtn.setTag("SalvoComSucesso");
        saveBtn.setOnClickListener(this);

        clearBtn.setText("Limpar");
        clearBtn.setTag("Limpar");
        clearBtn.setOnClickListener(this);

        linearLayout.addView(saveBtn);
        linearLayout.addView(clearBtn);
        linearLayout.addView(nomeAss);


        // retornando o layout
        return linearLayout;
    }

    // Onclick para Salvar ou Limpar
    @Override
    public void onClick(View v) {
        String tag = v.getTag().toString().trim();

        // salvar a assinatura
        if (tag.equalsIgnoreCase("SalvoComSucesso")) {
            this.saveImage(this.signatureView.getSignature());

        }
        // limpar a area
        else {
            this.signatureView.clearSignature();
        }

    }

    /**
     * salva a assinatura na mamemoria interna
     * @param signature bitmap
     */
    final void saveImage(Bitmap signature) {

        String root = Environment.getExternalStorageDirectory().toString();

        // diretorio onde sera salva
        File myDir = new File(root + "/saved_signature");

        // cria a pasta se nao existir
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        // ssetando um nome aleatorio para o arquivo
        Random r = new Random();
        int i = r.nextInt(80 - 65) + 65;
        String fname = i+".png";

        File file = new File(myDir, fname);
        try {

            // save the signature
            FileOutputStream out = new FileOutputStream(file);
            signature.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

            Toast.makeText(this.getContext(), "Assinatura salva com sucesso.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TView ond a asinatura será desenhada
     */
    private class SignatureView extends View {

        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public SignatureView(Context context) {

            super(context);

            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);

            this.setBackgroundColor(Color.WHITE);

            this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        }

        /**
         * Get signature
         *
         * @return
         */
        protected Bitmap getSignature() {

            Bitmap signatureBitmap = null;

            if (signatureBitmap == null) {
                signatureBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.RGB_565);
            }

            final Canvas canvas = new Canvas(signatureBitmap);
            this.draw(canvas);

            return signatureBitmap;
        }

        /**
         * limpando
         */
        private void clearSignature() {
            path.reset();
            this.invalidate();
        }

        // all touch events during the drawing
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(this.path, this.paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    path.moveTo(eventX, eventY);

                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);

                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:

                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }

    }

}