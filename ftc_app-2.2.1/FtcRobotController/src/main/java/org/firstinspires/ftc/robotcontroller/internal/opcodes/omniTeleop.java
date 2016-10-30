package org.firstinspires.ftc.robotcontroller.internal.opcodes;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import java.util.List;

public class omniTeleop extends OpMode implements SensorEventListener {
    DcMotor[] motors = new DcMotor[4];
    final double[] speeds = new double[]{0.3, 0.8};
    int speedIndex = 0;
    boolean lastPressed = false;

    Context context;
    SensorManager mSensorManager;
    List<Sensor> sensors;
    Sensor mRotVec;
    volatile String accuracyString;
    volatile float[] data;


    @Override
    public void init() {
        // Get the hardware from the robot configuration
        motors[0] = hardwareMap.dcMotor.get("wFront");
        motors[1] = hardwareMap.dcMotor.get("wBack");
        motors[2] = hardwareMap.dcMotor.get("wLeft");
        motors[3] = hardwareMap.dcMotor.get("wRight");

        //wFront.setDirection(DcMotorSimple.Direction.REVERSE);
        //register this opmode as a Sensor Listener, and register to listen for the Rotation Vector

        context = hardwareMap.appContext;
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        //get a list of all of the sensors available
        sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        //get a reference to the default Rotation Vector
        mRotVec = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


        mSensorManager.registerListener(this, mRotVec, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void loop() {

        motors[0].setPower(gamepad1.left_stick_y * speeds[speedIndex]);
        motors[1].setPower(-gamepad1.left_stick_y * speeds[speedIndex]);

        motors[2].setPower(gamepad1.left_stick_x * speeds[speedIndex]);
        motors[3].setPower(-gamepad1.left_stick_x * speeds[speedIndex]);

        if (gamepad1.right_trigger != 0) {
            for(DcMotor motor : motors)
                motor.setPower(gamepad1.right_trigger * speeds[speedIndex]);
        } else if (gamepad1.left_trigger != 0) {
            for(DcMotor motor : motors)
                motor.setPower(gamepad1.left_trigger * -speeds[speedIndex]);
        }

        try {
            if (gamepad1.left_bumper && !lastPressed) {
                speedIndex--;
            } else if (gamepad1.right_bumper && !lastPressed) {
                speedIndex++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            speedIndex = 1;
        }

        lastPressed = gamepad1.left_bumper || gamepad1.right_bumper;

        telemetry.addData("accuracy", "Accuracy: "+accuracyString);

        if(data != null) {
            for (int i = 0; i < data.length; i++) {
                telemetry.addData("data"+i, "Value " + i + ": " + data[i]);
            }
        } else {
            telemetry.addData("data", "No Data");
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor mSensor, int accuracy) {
        //get the current accuracy type, and convert to a string
        switch(accuracy) {
            case SensorManager.SENSOR_STATUS_NO_CONTACT:
                accuracyString = "No Contact";
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                accuracyString = "Unreliable";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                accuracyString = "Low";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                accuracyString = "Medium";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                accuracyString = "High";
                break;
        }

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        //get all of the data available
        data = event.values;
    }

    @Override
    public void stop() {
        //unregister the sensor event listener
        mSensorManager.unregisterListener(this);

    }
}

