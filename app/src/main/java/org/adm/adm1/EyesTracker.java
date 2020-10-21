package org.adm.adm1;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import java.sql.Time;
import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class EyesTracker extends Tracker<Face> {

    boolean olhosFechados= false;
    private Long inicioOlhosFechados;
    private Long tempoAcumulado;

    // definindo o threshold
    private final float THRESHOLD = 0.75f;

    // context
    private Context context;

    // construtor
    public EyesTracker(Context context) {
        this.context = context;
    }

    // atualizando dados de detecção
    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {

        if (face.getIsLeftEyeOpenProbability() > THRESHOLD || face.getIsRightEyeOpenProbability() > THRESHOLD) {
            ((MainActivity)context).updateMainView(Condition.USER_EYES_OPEN);
            olhosFechados = false;
        }else {
            if (!olhosFechados) {
                inicioOlhosFechados = System.currentTimeMillis();
                olhosFechados = true;
            }
            tempoAcumulado = System.currentTimeMillis() - inicioOlhosFechados;
            if (tempoAcumulado > 2000) { // aqui se verifica o tempo de olhos fechados
                ((MainActivity)context).updateMainView(Condition.ALARM);
            } else {
            ((MainActivity)context).updateMainView(Condition.USER_EYES_CLOSED);
            }
        }
    }

    // esse método é chamado quando a face não é encontrada
    @Override
    public void onMissing(Detector.Detections<Face> detections) {
        super.onMissing(detections);
        ((MainActivity)context).updateMainView(Condition.FACE_NOT_FOUND);
    }
    @Override
    public void onDone() {
        super.onDone();
    }
}
