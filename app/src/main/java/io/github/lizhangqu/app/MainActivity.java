package io.github.lizhangqu.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.apache.http.HttpEntity;
import org.apache.http.entity.BasicHttpEntity;

import java.io.IOException;

/**
 * @author lizhangqu
 * @version V1.0
 * @since 2018-11-08 17:08
 */
public class MainActivity extends AppCompatActivity {

    HttpEntity httpEntity=new BasicHttpEntity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpEntity httpEntity = new BasicHttpEntity();
        try {
            httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
