package com.nowfloats.Analytics_Screen.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nowfloats.Analytics_Screen.SocialAnalytics;
import com.nowfloats.NavigationDrawer.Create_Message_Activity;
import com.nowfloats.util.Methods;
import com.thinksity.R;


/**
 * Created by Abhi on 11/28/2016.
 */

public class PostFacebookUpdateFragment extends Fragment {
    Button postUpdate;
    Context context;
    String mType;

    public static Fragment getInstance(Bundle b){
        Fragment frag = new PostFacebookUpdateFragment();
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            Bundle b = getArguments();
            mType = b.getString("mType");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root =inflater.inflate(R.layout.fragment_facebook_create_update,container,false);
        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postUpdate= (Button) view.findViewById(R.id.create_update_button);
        TextView message = (TextView) view.findViewById(R.id.message);

        String socialType = null;
        if(SocialAnalytics.FACEBOOK.equals(mType)){
            socialType = "Facebook Page";
        }else if(SocialAnalytics.QUIKR.equals(mType)){
            socialType = "Quikr";
        }
        String text = "Looks like you haven\'t posted any update on your "+socialType+" through Boost yet Make sure you select the <b>"+socialType+" option</b> while creating an update";
        message.setText(Methods.fromHtml(text));
        postUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, Create_Message_Activity.class);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}
