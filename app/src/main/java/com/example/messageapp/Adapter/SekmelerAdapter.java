package com.example.messageapp.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.messageapp.Fragment.ChatFragment;
import com.example.messageapp.Fragment.GrupsFragment;
import com.example.messageapp.Fragment.PersonsFragment;
import com.example.messageapp.Fragment.RequestsFragment;

public class SekmelerAdapter extends FragmentPagerAdapter {
    public SekmelerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        //pozisyona göre hangi sekmenin açılacağı belirlenir
        switch (position)
        {
            case 0:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 1:
                GrupsFragment grupsFragment = new GrupsFragment();
                return grupsFragment;
            case 2:
                PersonsFragment personsFragment = new PersonsFragment();
                return personsFragment;
            case 3:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //pozisyona göre başlıklar
        switch (position)
        {
            case 0:
                return "Chats";
            case 1:
                return "Grups";
            case 2:
                return "Persons";
            case 3:
                return "Requests";
            default:
                return null;
        }
    }
}