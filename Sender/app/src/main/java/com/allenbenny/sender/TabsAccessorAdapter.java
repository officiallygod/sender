package com.allenbenny.sender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {

        switch (i){
            case 0: return new ChatsFragment();

            case 1: return new GroupsFragment();

            case 2: return new ContactsFragment();

            case 3: return new RequestsFragment();

            default: return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0: return "Chats";

            case 1: return "Groups";

            case 2: return "Contacts";

            case 3: return "Requests";

            default: return null;
        }

    }
}
