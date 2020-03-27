package com.example.albertz_business.model.login.manager;

import android.util.Log;

import com.example.albertz_business.model.login.user.User;
/**
 * @description 单例管理登陆用户信息
 */
public class UserManager {

        private static UserManager userManager = null;
        private User mUser = null;

        public static UserManager getInstance() {

            if (userManager == null) {

                synchronized (UserManager.class) {

                    if (userManager == null) {

                        userManager = new UserManager();
                    }
                    return userManager;
                }
            } else {

                return userManager;
            }
        }

        /**
         * init the user
         */
        public void setUser(User user) {

            mUser = user;
        }

        public boolean hasLogined() {

            return mUser != null;
        }

        /**
         * has user info
         */
        public User getUser() {
            Log.i("xxxx",""+mUser.emsg+mUser.data+mUser.ecode);
            return mUser;
        }

        /**
         * remove the user info
         */
        public void removeUser() {
            mUser = null;
        }

}
