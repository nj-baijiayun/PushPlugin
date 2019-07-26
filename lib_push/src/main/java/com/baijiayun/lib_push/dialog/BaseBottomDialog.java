/*
 *  -------------------------------------------------------------------------------------
 *  Mi-Me Confidential
 *
 *  Copyright (C) 2017.  Shanghai Mi-Me Financial Information Service Co., Ltd.
 *  All rights reserved.
 *
 *  No part of this file may be reproduced or transmitted in any form or by any means,
 *  electronic, mechanical, photocopying, recording, or otherwise, without prior
 *  written permission of Shanghai Mi-Me Financial Information Service Co., Ltd.
 *  -------------------------------------------------------------------------------------
 */

package com.baijiayun.lib_push.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager;

import com.dsg.lib_push.R;


/**
 * <p>write the description for the file
 *
 * @author houyi
 * @createTime 2016/7/22 11:04
 */
public class BaseBottomDialog extends Dialog {

    public BaseBottomDialog(Context context) {
        super(context, R.style.BasicBottomDialog);
        setDialogTheme();
    }

    private void setDialogTheme() {
        getWindow().setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);// android:backgroundDimEnabled默认是true的
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }
}
