package com.iindicar.indicar.b2_community.boardWrite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.commit451.teleprinter.Teleprinter;
import com.crashlytics.android.Crashlytics;
import com.iindicar.indicar.BaseActivity;
import com.iindicar.indicar.R;
import com.iindicar.indicar.data.vo.WriteBoardVO;
import com.iindicar.indicar.data.vo.WriteFileVO;
import com.iindicar.indicar.databinding.BoardWriteEditActivityBinding;
import com.iindicar.indicar.utils.DialogUtil;
import com.iindicar.indicar.utils.LocaleHelper;
import com.iindicar.indicar.utils.PickPhotoHelper;

import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by yeseul on 2018-05-06.
 */

public class BoardWriteEditActivity extends BaseActivity<BoardWriteEditActivityBinding> implements BoardWriteEditNavigator, View.OnClickListener {

    public static final int RESULT_CANCEL = 1;
    public static final int RESULT_UPLOAD_SUCCESS = 2;
    public static final int RESULT_UPDATE_SUCCESS = 3;

    public static final String EXTRA_BOARD_DETAIL = "BUNDLE_EXTRA_BOARD_DETAIL";
    public static final String EXTRA_BOARD_FILE_LIST = "BUNDLE_EXTRA_BOARD_FILES";
    public static final String BUNDLE_EXTRA_BOARD = "BUNDLE_EXTRA_BOARD";

    private final String FRAG_WRITE_FILTER = "FRAG_WRITE_FILTER";
    private final String FRAG_WRITE_ITEM = "FRAG_WRITE_ITEM";

    private BoardWriteEditViewModel viewModel;
    private boolean isUpdate = false;
    private String currFrag;

    private PickPhotoHelper pickPhotoHelper;
    private Teleprinter keyboard;

    Resources resources;

    @Override
    protected int getLayoutId() {
        return R.layout.board_write_edit_activity;
    }

    @Override
    protected void setActionBarImage(ObservableInt centerImageId, ObservableInt leftImageId) {
        centerImageId.set(R.drawable.logo_write);
        leftImageId.set(R.drawable.ic_action_close);
    }

    @Override
    public void onBackPressed() {
        if (currFrag == FRAG_WRITE_ITEM && !isUpdate)
            changeToWriteFilter();
        else
            onCancelWrite();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        Context boardWriteEditContext = LocaleHelper.setLocale(getApplicationContext());
        resources = boardWriteEditContext.getResources();

        viewModel = new BoardWriteEditViewModel(BoardWriteEditActivity.this);
        viewModel.setNavigator(this);

        binding.setViewModel(viewModel);
        binding.buttonNext.setOnClickListener(this);
        binding.buttonCancel.setOnClickListener(this);

        pickPhotoHelper = new PickPhotoHelper(this);

        initView();

        initKeyboard();
    }

    private void initKeyboard() {
        keyboard = new Teleprinter(this);
        keyboard.addKeyboardToggleListener(new Teleprinter.OnKeyboardToggleListener() {
            @Override
            public void onKeyboardShown(int keyboardSize) {
                if (FRAG_WRITE_ITEM.equals(currFrag)) {
                    binding.buttonContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onKeyboardClosed() {
                if (FRAG_WRITE_ITEM.equals(currFrag)) {
                    binding.buttonContainer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initView() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(BUNDLE_EXTRA_BOARD);

        //언어별 뷰 셋팅
        binding.buttonNext.setImageDrawable(resources.getDrawable(R.drawable.btn_next));
        binding.buttonSubmit.setImageDrawable(resources.getDrawable(R.drawable.btn_upload_board));
        binding.buttonCancel.setImageDrawable(resources.getDrawable(R.drawable.btn_quit));

        if (bundle == null) { // write new board
            // get login user id, name from sharedPreferences
            SharedPreferences prefLogin = getSharedPreferences("prefLogin", Context.MODE_PRIVATE);
            String loginId = prefLogin.getString("id", "");
            String loginName = prefLogin.getString("name", "");
            viewModel.start(loginId, loginName);

            changeToWriteFilter();


        } else { // update board
            isUpdate = true;
            WriteBoardVO vo = bundle.getParcelable(EXTRA_BOARD_DETAIL);
            List<WriteFileVO> items = bundle.getParcelableArrayList(EXTRA_BOARD_FILE_LIST);
            viewModel.start(vo, items);
            viewModel.boardType.set(vo.getBoardType());

            changeToWriteItem();
        }
    }

    @Override
    public void changeToWriteItem() {
        if (viewModel.boardType.get() == null) {
            showSnackBar(resources.getString(R.string.selectCategory));
            return;
        }
        currFrag = FRAG_WRITE_ITEM;
        viewModel.currentPageNum = 0;
        viewModel.currentPage.set(0);
        BoardWriteItemFragment fragment = new BoardWriteItemFragment();
        fragment.setViewModel(viewModel);
        fragment.setPickPhotoHelper(pickPhotoHelper);
        fragment.setKeyboard(keyboard);
        changeFragment(getSupportFragmentManager(), fragment);

        changeActionbarButton(FRAG_WRITE_ITEM);
        binding.buttonNext.setVisibility(View.GONE);
        binding.buttonContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void changeToWriteFilter() {
        currFrag = FRAG_WRITE_FILTER;

        BoardWriteFilterFragment fragment = new BoardWriteFilterFragment();
        fragment.setViewModel(viewModel);
        changeFragment(getSupportFragmentManager(), fragment);

        changeActionbarButton(FRAG_WRITE_FILTER);
        binding.buttonNext.setVisibility(View.VISIBLE);
        binding.buttonContainer.setVisibility(View.GONE);
    }

    private void changeActionbarButton(String tag) {
        if (FRAG_WRITE_FILTER.equals(tag)) {
            leftImageId.set(R.drawable.ic_action_close);
            actionBarBinding.buttonLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCancelWrite();
                }
            });

        } else if (FRAG_WRITE_ITEM.equals(tag)) {
            if (viewModel.isNewBoard) {
                leftImageId.set(R.drawable.btn_back);
                actionBarBinding.buttonLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeToWriteFilter();
                    }
                });
            } else {
                leftImageId.set(R.drawable.ic_action_close);
                actionBarBinding.buttonLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onCancelWrite();
                    }
                });
            }
        }
    }

    private void changeFragment(FragmentManager fragmentManager, Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }


    @Override
    public void onBoardUpdated() {

        setResult(RESULT_UPDATE_SUCCESS);
        finish();
        overridePendingTransition(R.anim.enter_no_anim, R.anim.exit_no_anim);
    }

    @Override
    public void onBoardUploaded() {
        Intent intent = new Intent();
        intent.putExtra("isUpdated", true);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.enter_no_anim, R.anim.exit_no_anim);
    }

    @Override
    public void onCancelWrite() {

        DialogUtil.showDialog(this,
                resources.getString(R.string.stopWriting),
                resources.getString(R.string.stopWritingSub),
                0.9, 0.25,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onActivityFinish();
                    }
                });
    }

    @Override
    public void onActivityFinish() {
        finish();
        overridePendingTransition(R.anim.enter_no_anim, R.anim.exit_no_anim);
    }

    @Override
    public void showSnackBar(String text) {
        Snackbar.make(binding.getRoot(), "" + text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void pbOn() {
        binding.pbWrite.setVisibility(View.VISIBLE);
        binding.buttonSubmit.setEnabled(false);
    }

    @Override
    public void pbOff() {
        binding.buttonSubmit.setEnabled(true);
        binding.pbWrite.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_next:
                changeToWriteItem();
                break;
            case R.id.button_cancel:
                if (currFrag == FRAG_WRITE_ITEM && !isUpdate)
                    changeToWriteFilter();
                else
                    onCancelWrite();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pickPhotoHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();

        //언어별 뷰 셋팅
        binding.buttonNext.setImageDrawable(resources.getDrawable(R.drawable.btn_next));
        binding.buttonSubmit.setImageDrawable(resources.getDrawable(R.drawable.btn_upload_board));
        binding.buttonCancel.setImageDrawable(resources.getDrawable(R.drawable.btn_quit));
    }
}
