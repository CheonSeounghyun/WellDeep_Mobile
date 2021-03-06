package com.example.project3;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    private ImageView iv_img;
    Button btn_play, btn_stop, btn_call, btn_delete;
    MediaPlayer player;
    String img_url;
    String voice_url;
    int position = 0; // 다시 시작 기능을 위한 현재 재생 위치 확인 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        iv_img = findViewById(R.id.iv_img);

        btn_play = findViewById(R.id.btn_play);
        btn_stop = findViewById(R.id.btn_stop);
        btn_call = findViewById(R.id.btn_call);
        btn_delete = findViewById(R.id.btn_delete);

        Intent intent = getIntent();

        final String num = intent.getExtras().getString("num"); // 클릭한 알람 번호 가져오기
        final String id = intent.getExtras().getString("loginid");
        final String pw = intent.getExtras().getString("pw");
        final String addr = intent.getExtras().getString("addr");
        final String phone = intent.getExtras().getString("phone");
        final String name = intent.getExtras().getString("name");
        final String sex = intent.getExtras().getString("sex");
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudio();
            }
        });

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(("tel:010-1111-2222")));
                startActivity(intent);
            }
        });

        final String alarm_num = num;

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String result  = new DetailActivity.CustomTask().execute(alarm_num,"delete").get();
                    Log.d("받아온 값", result);

                    if (result.contains("0")) {
                        Toast.makeText(DetailActivity.this, "삭제실패", Toast.LENGTH_SHORT).show();
                    } else if(result == null) {
                        Toast.makeText(DetailActivity.this,"삭제실패", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DetailActivity.this, "삭제성공", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                        intent.putExtra("loginid",id);
                        intent.putExtra("pw",pw);
                        intent.putExtra("addr",addr);
                        intent.putExtra("phone",phone);
                        intent.putExtra("name",name);
                        intent.putExtra("sex",sex);
                        startActivity(intent);
                        finish();
                    }

                } catch (Exception e) {
                    Toast.makeText(DetailActivity.this, "삭제 실패했음", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // 알람 번호 전송-> 자녀 리스트 받아오기
        try {
            String result  = new DetailActivity.CustomTask().execute(alarm_num,"child_list").get();
            Log.d("받아온 값", result);

            JSONObject jsonObject = new JSONObject(result); //result를 인자로 넣어 jsonObject를 생성한다.

            JSONArray jsonArray = jsonObject.getJSONArray("dataSet"); //"dataSet"의 jsonObject들을 배열로 저장한다.

            for(int i=0; i<jsonArray.length(); i++) { //jsonObject에 담긴 두 개의 jsonObject를 jsonArray를 통해 하나씩 호출한다.
                jsonObject = jsonArray.getJSONObject(i);
                img_url = "http://192.168.56.1:8081/WellDeep/alarm/" + jsonObject.getString("i_file"); // 이미지파일 가져오기
                voice_url = "http://192.168.56.1:8081/WellDeep/voice/" + jsonObject.getString("v_file"); // 음성파일 가져오기
            }
            // Glide로 이미지 표시하기
            Glide.with(this).load(img_url).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(iv_img);
        }catch (Exception e) {
            Toast.makeText(DetailActivity.this,"오류발생",Toast.LENGTH_SHORT).show();
        }

    }
    // 알람 번호 서버에 전송
    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        // doInBackground의 매개값이 문자열 배열인데요. 보낼 값이 여러개일 경우를 위해 배열로 합니다.
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://192.168.56.1:8081/WellDeep/Alarm_get_android.jsp"); //보낼 jsp 주소를 ""안에 작성합니다.
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터를 POST 방식으로 전송합니다.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "num="+strings[0]+"&type="+strings[1];//보낼 정보인데요. GET방식으로 작성합니다. ex) "id=rain483&pwd=1234";
                Log.e("send",sendMsg);
                //회원가입처럼 보낼 데이터가 여러 개일 경우 &로 구분하여 작성합니다.
                osw.write(sendMsg);//OutputStreamWriter에 담아 전송합니다.
                osw.flush();
                //jsp와 통신이 정상적으로 되었을 때 할 코드들입니다.
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    //jsp에서 보낸 값을 받겠죠?
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode()+"에러");
                    // 통신이 실패했을 때 실패한 이유를 알기 위해 로그를 찍습니다.
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //jsp로부터 받은 리턴 값입니다.
            return receiveMsg;
        }
    }

    private void playAudio() {
        try {
            closePlayer();

            player = new MediaPlayer();
            player.setDataSource(voice_url); // 음성파일 경로
            player.prepare();
            player.start();

            Toast.makeText(this, "재생 시작됨.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void stopAudio() {
        if (player != null && player.isPlaying()) {
            player.stop();

            Toast.makeText(this, "정지됨.", Toast.LENGTH_SHORT).show();
        }
    }

    /* 녹음 시 마이크 리소스 제한. 누군가가 lock 걸어놓으면 다른 앱에서 사용할 수 없음.
     * 따라서 꼭 리소스를 해제해주어야함. */
    public void closePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

}