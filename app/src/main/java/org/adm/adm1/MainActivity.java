package org.adm.adm1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;
import java.io.IOException;

public class MainActivity<L> extends AppCompatActivity {

    ConstraintLayout background;
    TextView user_message;
    boolean trocarCamera = true;
    boolean flag = false;
    CameraSource cameraSource;

    Long tempoInicial = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // verifica permissão da câmera
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Permissão não autorizada!\n Autorize o uso e reinicie o app.", Toast.LENGTH_SHORT).show();
        } else {
            init();
        }
    }
        private void init(){
            background = findViewById(R.id.background);
            user_message = findViewById(R.id.user_text);
            flag = true;
            initCameraSource();
        }

    private void initCameraSource() {

        // cria um detector de faces
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        detector.setProcessor(new MultiProcessor.Builder(new FaceTrackerDaemon(MainActivity.this)).build());

        if (trocarCamera) {
            cameraSource = new CameraSource.Builder(this, detector)
                    .setRequestedPreviewSize(1024, 768)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedFps(30.0f)
                    .build();
            Toast.makeText(this, "Câmera frontal acionada", Toast.LENGTH_LONG).show();
        } else {
            cameraSource = new CameraSource.Builder(this, detector)
                    .setRequestedPreviewSize(1024, 768)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(30.0f)
                    .build();
            Toast.makeText(this, "Câmera traseira acionada", Toast.LENGTH_LONG).show();
        }

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraSource.start();
        }
        catch (IOException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraSource != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                cameraSource.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraSource!=null) {
            cameraSource.stop();
        }
        setBackgroundGrey();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource!=null) {
            cameraSource.release();
        }
    }

    boolean apitando = false;
    double tempoAlarme;
    Long tempoInicioAlarme = null;

    public void updateMainView(Condition condition){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        switch (condition){
            case START:
                break;
            case USER_EYES_OPEN:
                setBackgroundGreen();
                user_message.setText("Olhos abertos!");
                try {
                    r.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tempoInicial = System.currentTimeMillis();
                break;
            case USER_EYES_CLOSED:
                setBackgroundOrange();
                user_message.setText("Fechados!");
                break;
            case FACE_NOT_FOUND:
                setBackgroundRed();
                user_message.setText("Sem rosto!");
                break;
            case ALARM:
                setBackgroundBlue();
                user_message.setText("Alarme!");
                try {
                    if (!apitando) {
                        r.play();
                        apitando = true;
                        tempoInicioAlarme = System.currentTimeMillis();
                    }
                    tempoAlarme = System.currentTimeMillis() - tempoInicioAlarme;
                    if (apitando && tempoAlarme > 1000 ) { // evita efeito eco do alarme
                        r.stop();
                        apitando = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                setBackgroundBlue();
                user_message.setText("Bem-vindo!");
        }
    }

    //pinta o fundo de cinza
    private void setBackgroundGrey() {
        if(background != null)
            background.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
    }

    //pinta o fundo de verde
    private void setBackgroundGreen() {
        if(background != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    background.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            });
        }
    }

    //pinta o fundo de laranja
    private void setBackgroundOrange() {
        if(background != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    background.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                }
            });
        }
    }

    //pinta o fundo de vermelho
    private void setBackgroundRed() {
        if(background != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    background.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            });
        }
    }

    //pinta o fundo de azul
    private void setBackgroundBlue() {
        if(background != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    background.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_foreground);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.itemTrocarCamera:
                trocarCamera = !trocarCamera;
                cameraSource.release();
                init();
                break;
            case R.id.itemUmBomSono:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setIcon(R.drawable.ic_bom_sono);
                dialog.setTitle("7 medidas para dormir bem");
                dialog.setMessage("\n1. Buscar dormir em um ambiente escuro, silencioso, bem ventilado e com temperatura agradável;\n\n" +
                        "2. Ter horário regular para levantar e deitar sete dias por semana;\n\n" +
                        "3. Não dormir com fome, evitando refeições pesadas até três horas antes de dormir;\n\n" +
                        "4. Evitar bebidas com cafeína à noite, pois o café, chá preto, chimarrão e refrigerantes à base de cola estimulam o sistema nervoso;\n\n" +
                        "5. Dormir apenas o necessário para sentir-se recuperado, pois muito tempo na cama interfere na qualidade do sono na noite seguinte;\n\n" +
                        "6. Evitar fumar e beber, principalmente à noite, pois pode prejudicar a saúde, estimular o ronco e causar sonhos desagradáveis;\n\n" +
                        "7. Diante de eventual noite de insônia não se deve permanecer na cama forçando o sono. Deve-se procurar uma atividade fora da cama e só se deitar quando sentir novamente sono.");
                dialog.setCancelable(false);
                dialog.setPositiveButton("OK",null);
                dialog.create();
                dialog.show();
                break;
            case R.id.itemSobre:
                AlertDialog.Builder dialog2 = new AlertDialog.Builder(this);
                dialog2.setIcon(R.drawable.ic_sobre);
                dialog2.setTitle("Sobre o AcordApp");
                dialog2.setMessage("\nObjetivo: evitar acidentes de trânsito devido à sonolência ao volante.\n\n" +
                        "Forma de usar: aumentar o volume, selecionar a câmera adequada e prender o celular no painel do carro.\n\n" +
                        "Realização: Trabalho de Conclusão do curso de Ciência da Computação.\n" +
                        "Faculdade: Instituto de Educação Superior de Brasília (IESB).\n" +
                        "Orientadora: Profa. Dra. Letícia Zoby.\n" +
                        "Autor: Gabriel Rodrigues Ibañez.\n" +
                        "E-mail: gabrielibanezsql@gmail.com\n\n" +
                        "Outubro de 2020 - Utilização livre.");
                dialog2.setCancelable(false);
                dialog2.setPositiveButton("OK",null);
                dialog2.create();
                dialog2.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
