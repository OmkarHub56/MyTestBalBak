package com.myapps.bakbak.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.myapps.bakbak.Fragments.ChatsFragment;
import com.myapps.bakbak.Fragments.ReceivedFragment;
import com.myapps.bakbak.Fragments.SentFragment;

public class FragmentAdapter extends FragmentStateAdapter {

    SentFragment frg;
    Context context;
    public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,Context context) {
        super(fragmentManager, lifecycle);
        this.context=context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position==0){
            return new ChatsFragment();
        }
        else if(position==1){
            frg=new SentFragment();
            return frg;
        }
        else if(position==2){
            return new ReceivedFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public SentFragment getSentFragment(){
        return frg;
    }
}
