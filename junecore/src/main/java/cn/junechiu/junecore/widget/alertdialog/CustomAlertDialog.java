
package cn.junechiu.junecore.widget.alertdialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import cn.junechiu.junecore.R;

public class CustomAlertDialog extends Dialog {

    public TextView dialogTitle, dialogTitleDes; // 显示电话信息

    public TextView dialogBtnCancel; // 取消

    public TextView dialogBtnOk; // 确认

    public CustomAlertDialog(Context context, final String showTitle, String titleDes,
                             String okButtonText, final CallBackInterface callBackInterface) {
        super(context, R.style.alert_dialog);
        setContentView(R.layout.custom_alert_dialog);
        dialogTitle = findViewById(R.id.dialog_title);
        dialogTitleDes = findViewById(R.id.dialog_title_des);
        dialogBtnCancel = findViewById(R.id.dialog_btn_cancel);
        dialogBtnOk = findViewById(R.id.dialog_btn_ok);

        // 主标题
        dialogTitle.setText(showTitle);

        // 副标题
        if (!TextUtils.isEmpty(titleDes)) {
            dialogTitleDes.setVisibility(View.VISIBLE);
            dialogTitleDes.setText(titleDes);
        } else {
            dialogTitleDes.setVisibility(View.GONE);
        }

        // 确认按钮文字
        if (!TextUtils.isEmpty(okButtonText)) {
            dialogBtnOk.setText(okButtonText);
        }

        dialogBtnCancel.setOnClickListener(v -> {
            if (cancelListener != null) {
                cancelListener.onCancel();
            }
            dismiss();
        });

        dialogBtnOk.setOnClickListener(v -> {
            if (callBackInterface != null) {
                callBackInterface.callBackFunction(showTitle);
            }
            dismiss();
        });
    }

    public void setDesColorText(String source) {
        dialogTitleDes.setVisibility(View.VISIBLE);
        dialogTitleDes.setTextSize(13);
        dialogTitleDes.setText(Html.fromHtml(source));
    }

    private OnCancelListener cancelListener;

    public void setCancelListener(OnCancelListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    public interface OnCancelListener {
        void onCancel();
    }
}
