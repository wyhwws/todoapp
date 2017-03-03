/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.addedittask;

import static com.google.common.base.Preconditions.checkNotNull;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.architecture.blueprints.todoapp.R;
import com.sw926.imagefileselector.ErrorResult;
import com.sw926.imagefileselector.ImageCropper;
import com.sw926.imagefileselector.ImageFileSelector;

import java.io.File;

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
public class AddEditTaskFragment extends Fragment implements AddEditTaskContract.View,View.OnClickListener {

    public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID";
    public static final String ARGUMENT_EDIT_TASK_INTERNAL_ID = "EDIT_TASK_INTERNAL_ID";

    private AddEditTaskContract.Presenter mPresenter;

    private TextView mTitle;

    private TextView mDescription;
    private TextView mImage;
    private ImageView mImageView;
    private TextView mTvPath;
    private ImageFileSelector mImageFileSelector;

    private EditText mEtWidth;
    private EditText mEtHeight;

    private ImageCropper mImageCropper;
    private Button mBtnCrop;

    private File mCurrentSelectFile;

    public AddEditTaskFragment() {
        // Required empty public constructor
    }

    public static AddEditTaskFragment newInstance() {
        return new AddEditTaskFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(@NonNull AddEditTaskContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.saveTask(mTitle.getText().toString(), mDescription.getText().toString(),mTvPath.toString());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.addtask_frag, container, false);
        mTitle = (TextView) root.findViewById(R.id.add_task_title);
        mDescription = (TextView) root.findViewById(R.id.add_task_description);
        mImage = (TextView) root.findViewById(R.id.add_task_image);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_from_sdcard).setOnClickListener(this);
        view.findViewById(R.id.btn_from_camera).setOnClickListener(this);
        view.findViewById(R.id.btn_crop).setOnClickListener(this);


        mImageView = (ImageView) view.findViewById(R.id.iv_image);
        mTvPath = (TextView) view.findViewById(R.id.tv_path);
        mEtWidth = (EditText) view.findViewById(R.id.et_width);
        mEtHeight = (EditText) view.findViewById(R.id.et_height);
        mBtnCrop = (Button) view.findViewById(R.id.btn_crop);

        mImageFileSelector = new ImageFileSelector(getContext());
        mImageFileSelector.setCallback(new ImageFileSelector.Callback() {
            @Override
            public void onError(ErrorResult errorResult) {
                switch (errorResult) {
                    case permissionDenied:
                        Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                        break;
                    case canceled:
                        Toast.makeText(getContext(), "Canceled", Toast.LENGTH_LONG).show();
                        break;
                    case error:
                        Toast.makeText(getContext(), "Unknown Error", Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onSuccess(String file) {
                loadImage(file);
                mCurrentSelectFile = new File(file);
                mBtnCrop.setVisibility(View.VISIBLE);
            }
        });
        mImageCropper = new ImageCropper();
        mImageCropper.setCallback(new ImageCropper.ImageCropperCallback() {
            @Override
            public void onError(ImageCropper.CropperErrorResult result) {
                switch (result) {
                    case error:
                        Toast.makeText(getContext(), "crop image error", Toast.LENGTH_LONG).show();
                        break;
                    case canceled:
                        Toast.makeText(getContext(), "crop image canceled", Toast.LENGTH_LONG).show();
                        break;
                    case notSupport:
                        Toast.makeText(getContext(), "crop image not support", Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onSuccess(String outputFile) {
                loadImage(outputFile);
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_from_camera: {
                initImageFileSelector();
                mImageFileSelector.takePhoto(this, 1);
                break;
            }
            case R.id.btn_from_sdcard: {
                initImageFileSelector();
                mImageFileSelector.selectImage(this, 2);
                break;
            }
            case R.id.btn_crop: {
                if (mCurrentSelectFile != null) {
                    mImageCropper.setOutPut(800, 800);
                    mImageCropper.setOutPutAspect(1, 1);
                    mImageCropper.cropImage(this, mCurrentSelectFile.getPath(), 3);
                }
                break;
            }
        }
    }
    private void initImageFileSelector() {
        int w = 0;
        if (!TextUtils.isEmpty(mEtWidth.getText().toString())) {
            w = Integer.parseInt(mEtWidth.getText().toString());
        }
        int h = 0;
        if (!TextUtils.isEmpty(mEtHeight.getText().toString())) {
            h = Integer.parseInt(mEtHeight.getText().toString());
        }
        mImageFileSelector.setOutPutImageSize(w, h);
    }
    private void loadImage(final String file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = BitmapFactory.decodeFile(file);
                File imageFile = new File(file);
                final StringBuilder builder = new StringBuilder();
                builder.append("path: ");
                builder.append(file);
                builder.append("\n\n");
                builder.append("length: ");
                builder.append((int) (imageFile.length() / 1024d));
                builder.append("KB");
                builder.append("\n\n");
                builder.append("image size: (");
                builder.append(bitmap.getWidth());
                builder.append(", ");
                builder.append(bitmap.getHeight());
                builder.append(")");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(bitmap);
                        mTvPath.setText(builder.toString());
                        mImage.setText(file.toString());
                    }
                });
            }
        }).start();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImageFileSelector.onActivityResult(getContext(), requestCode, resultCode, data);
        mImageCropper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mImageFileSelector.onSaveInstanceState(outState);
        mImageCropper.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
     //   mImageFileSelector.onRestoreInstanceState(savedInstanceState);
       // mImageCropper.onRestoreInstanceState(savedInstanceState);
    }
    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mImageFileSelector.onRequestPermissionsResult(getContext(), requestCode, permissions, grantResults);
    }
    @Override
    public void showEmptyTaskError() {
        Snackbar.make(mTitle, getString(R.string.empty_task_message), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showTasksList() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setDescription(String description) {
        mDescription.setText(description);
    }

    @Override
    public void setImage(String image) {
     //  mImage.setText(image);
    }
}
