package com.integreight.onesheeld.shields.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.appFragments.ShieldsOperations;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.utils.customviews.ComboSeekBar;
import com.integreight.onesheeld.utils.customviews.OneSheeldToggleButton;

import java.util.Arrays;

public class ColorDetectionFragment extends ShieldFragmentParent<ColorDetectionFragment> implements ColorDetectionShield.ColorDetectionEventHandler, ShieldsOperations.OnChangeListener, MainActivity.OnSlidingMenueChangeListner {
    private View normalColor;
    private LinearLayout fullColor;
    private View colorsContainer;
    private OneSheeldToggleButton operationToggle;
    private OneSheeldToggleButton scaleToggle;
    private OneSheeldToggleButton typeToggle;
    private CheckBox frontBackPreviewToggle;
    private CheckBox cameraPreviewToggle;
    private ComboSeekBar scaleSeekBar;
    private ComboSeekBar patchSizeSeekBar;
    private String[] grayScale = new String[]{"1 Bit", "2 Bit", "4 Bit", "8 Bit"};
    private String[] rgbScale = new String[]{"3 Bit", "6 Bit", "9 Bit", "12 Bit", "15 Bit", "18 Bit", "24 Bit"};
    private String[] patchSizes = new String[]{"Small", "Medium", "Large"};
    private int[] colorsViewLocation = new int[2];
    Window window;
    Rect rectangle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.color_detection_shield_fragment_layout, container,
                false);
        if (getAppActivity().getSupportFragmentManager().findFragmentByTag(ShieldsOperations.class.getName()) != null)
            ((ShieldsOperations) getAppActivity().getSupportFragmentManager().findFragmentByTag(ShieldsOperations.class.getName())).addOnSlidingLocksListener(this);
        activity.registerSlidingMenuListner(this);
        setHasOptionsMenu(true);
        return v;
    }

    private void applyListeners() {
        operationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).setRecevedFramesOperation(ColorDetectionShield.RECEIVED_FRAMES.CENTER);
                    notifyNormalColor();
                } else {
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).setRecevedFramesOperation(ColorDetectionShield.RECEIVED_FRAMES.NINE_FRAMES);
                    notifyFullColor();
                }
            }
        });
        scaleToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).setCurrentPallete(ColorDetectionShield.ColorPalette._24_BIT_RGB);
                } else {
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).setCurrentPallete(ColorDetectionShield.ColorPalette._8_BIT_GRAYSCALE);
                }
                scaleSeekBar.setAdapter(Arrays.asList(b ? rgbScale : grayScale));
                scaleSeekBar.setSelection((b ? rgbScale : grayScale).length - 1);
            }
        });
        typeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((ColorDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).setColorType(b ? ColorDetectionShield.COLOR_TYPE.COMMON : ColorDetectionShield.COLOR_TYPE.AVERAGE);

            }
        });
        scaleSeekBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((ColorDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).setCurrentPallete(ColorDetectionShield.ColorPalette.get((byte) ((scaleToggle.isChecked() ? 4 : 0) + 1 + i)));
            }
        });
        patchSizeSeekBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((ColorDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).setPatchSize(i == 0 ? ColorDetectionShield.PATCH_SIZE.SMALL : i == 1 ? ColorDetectionShield.PATCH_SIZE.MEDIUM : ColorDetectionShield.PATCH_SIZE.LARGE);
            }
        });
        frontBackPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean feeback = ((ColorDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).setCameraToPreview(b);
                if (!feeback) {
                    removeListeners();
                    frontBackPreviewToggle.setChecked(!b);
                    applyListeners();
                }
            }
        });
        cameraPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!((CheckBox) activity.findViewById(R.id.isMenuOpening)).isChecked())
                        ((CheckBox) activity.findViewById(R.id.isMenuOpening)).setChecked(true);
                    else {
                        Rect rectangle = new Rect();
                        Window window = getActivity().getWindow();
                        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
                        colorsContainer.getLocationInWindow(colorsViewLocation);
                        ((ColorDetectionShield) getApplication().getRunningShields().get(
                                getControllerTag())).showPreview(colorsViewLocation[0], colorsViewLocation[1] - rectangle.top, colorsContainer.getWidth(), colorsContainer.getHeight());
                    }
                } else
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).hidePreview();
            }
        });
    }

    private void removeListeners() {
        operationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });
        scaleToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });
        typeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });
        scaleSeekBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        patchSizeSeekBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        frontBackPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
        cameraPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        normalColor = view.findViewById(R.id.normalColor);
        fullColor = (LinearLayout) view.findViewById(R.id.fullColor);
        operationToggle = (OneSheeldToggleButton) view.findViewById(R.id.operation);
        scaleToggle = (OneSheeldToggleButton) view.findViewById(R.id.scale);
        cameraPreviewToggle = (CheckBox) view.findViewById(R.id.camera_preview_toggle);
        typeToggle = (OneSheeldToggleButton) view.findViewById(R.id.type);
        frontBackPreviewToggle = (CheckBox) view.findViewById(R.id.frontBackToggle);
        scaleSeekBar = (ComboSeekBar) view.findViewById(R.id.scaleSeekBar);
        patchSizeSeekBar = (ComboSeekBar) view.findViewById(R.id.patchSeekBar);
        colorsContainer = view.findViewById(R.id.colorsContainer);
        ((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).setColorEventHandler(this);
    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        removeListeners();
        if (((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getRecevedFramesOperation() == ColorDetectionShield.RECEIVED_FRAMES.CENTER)
            enableNormalColor();
        else
            enableFullColor();
        scaleToggle.setChecked(!((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getCurrentPallete().isGrayscale());
        typeToggle.setChecked(((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getColorType() == ColorDetectionShield.COLOR_TYPE.COMMON);
        scaleSeekBar.setAdapter(Arrays.asList(scaleToggle.isChecked() ? rgbScale : grayScale));
        scaleSeekBar.setSelection(((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getCurrentPallete().getIndex());
        patchSizeSeekBar.setAdapter(Arrays.asList(patchSizes));
        patchSizeSeekBar.setSelection(((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getPatchSize() == ColorDetectionShield.PATCH_SIZE.SMALL ? 0 : ((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getPatchSize() == ColorDetectionShield.PATCH_SIZE.MEDIUM ? 1 : 2);
        applyListeners();
        super.onStart();

    }


    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                    if (((CheckBox) activity.findViewById(R.id.isMenuOpening)).isChecked() && !activity.isMenuOpened() && cameraPreviewToggle.isChecked()) {
                        rectangle = new Rect();
                        window = getActivity().getWindow();
                        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
                        colorsContainer.getLocationInWindow(colorsViewLocation);
                        ((ColorDetectionShield) getApplication().getRunningShields().get(
                                getControllerTag())).showPreview(colorsViewLocation[0], colorsViewLocation[1] - rectangle.top, colorsContainer.getWidth(), colorsContainer.getHeight());
                    } else
                        ((ColorDetectionShield) getApplication().getRunningShields().get(
                                getControllerTag())).hidePreview();
                }
            }
        }, 500);
        removeListeners();
        frontBackPreviewToggle.setChecked(((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).isBackPreview());
        applyListeners();
    }

    @Override
    public void onPause() {
        if (getApplication().getRunningShields().get(
                getControllerTag()) != null)
            ((ColorDetectionShield) getApplication().getRunningShields().get(
                    getControllerTag())).hidePreview();
        getView().invalidate();
        super.onPause();
    }

    @Override
    public void onColorChanged(final int... color) {
        if (normalColor != null && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            if (((ColorDetectionShield) getApplication().getRunningShields().get(
                    getControllerTag())).getRecevedFramesOperation() == ColorDetectionShield.RECEIVED_FRAMES.CENTER && color.length > 0) {
                uiHandler.removeCallbacks(null);
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        normalColor.setBackgroundColor(color[0]);
                    }
                });
            } else if (((ColorDetectionShield) getApplication().getRunningShields().get(
                    getControllerTag())).getRecevedFramesOperation() == ColorDetectionShield.RECEIVED_FRAMES.NINE_FRAMES && color.length > 0) {
                int k = 0;
                for (int i = 0; i < fullColor.getChildCount(); i++) {
                    for (int j = 0; j < ((LinearLayout) fullColor.getChildAt(i)).getChildCount(); j++) {
                        final View cell = ((LinearLayout) fullColor.getChildAt(i)).getChildAt(j);
                        final int m = k;
                        cell.removeCallbacks(null);
                        cell.post(new Runnable() {
                            @Override
                            public void run() {
                                if (m < color.length) {
//                                    Log.d("logColor", color.length + "   " + m + "   " + color[m]);
                                    cell.setBackgroundColor(color[m]);
                                }
                            }
                        });
                        k++;
                    }
                }
            }
        }
    }

    public void notifyFullColor() {
        if (normalColor != null && getView() != null) {
            normalColor.post(new Runnable() {
                @Override
                public void run() {
                    removeListeners();
                    normalColor.setVisibility(View.INVISIBLE);
                    fullColor.setVisibility(View.VISIBLE);
                    applyListeners();
                }
            });
        }
    }

    public void notifyNormalColor() {
        if (normalColor != null && getView() != null) {
            normalColor.post(new Runnable() {
                @Override
                public void run() {
                    removeListeners();
                    normalColor.setVisibility(View.VISIBLE);
                    fullColor.setVisibility(View.INVISIBLE);
                    applyListeners();
                }
            });
        }
    }

    @Override
    public void enableFullColor() {
        if (operationToggle != null && getView() != null) {
            notifyFullColor();
            operationToggle.post(new Runnable() {
                @Override
                public void run() {
                    removeListeners();
                    if (operationToggle != null && getView() != null)
                        operationToggle.setChecked(false);
                    applyListeners();
                }
            });
        }
    }

    @Override
    public void enableNormalColor() {
        if (operationToggle != null && getView() != null) {
            notifyNormalColor();
            operationToggle.post(new Runnable() {
                @Override
                public void run() {
                    removeListeners();
                    if (operationToggle != null && getView() != null)
                        operationToggle.setChecked(true);
                    applyListeners();
                }
            });
        }
    }

    @Override
    public void setPallete(final ColorDetectionShield.ColorPalette pallete) {
        if (scaleToggle != null && getView() != null) {
            scaleToggle.post(new Runnable() {
                @Override
                public void run() {
                    if (scaleToggle != null && getView() != null) {
                        removeListeners();
                        scaleToggle.setChecked(!pallete.isGrayscale());
                        scaleSeekBar.setAdapter(Arrays.asList(scaleToggle.isChecked() ? rgbScale : grayScale));
                        scaleSeekBar.setSelection(pallete.getIndex());
                        applyListeners();
                    }
                }
            });
        }
    }

    @Override
    public void changeCalculationMode(final ColorDetectionShield.COLOR_TYPE type) {
        if (typeToggle != null && getView() != null) {
            typeToggle.post(new Runnable() {
                @Override
                public void run() {
                    removeListeners();
                    typeToggle.setChecked(type == ColorDetectionShield.COLOR_TYPE.COMMON);
                    applyListeners();
                }
            });
        }
    }

    @Override
    public void changePatchSize(final ColorDetectionShield.PATCH_SIZE patchSize) {
        if (patchSizeSeekBar != null && getView() != null) {
            patchSizeSeekBar.post(new Runnable() {
                @Override
                public void run() {
                    removeListeners();
                    patchSizeSeekBar.setSelection(patchSize == ColorDetectionShield.PATCH_SIZE.SMALL ? 0 : patchSize == ColorDetectionShield.PATCH_SIZE.MEDIUM ? 1 : 2);
                    applyListeners();
                }
            });
        }
    }

    @Override
    public void onCameraPreviewTypeChanged(final boolean isBack) {
        if (canChangeUI() && getView() != null && frontBackPreviewToggle != null)
            frontBackPreviewToggle.post(new Runnable() {
                @Override
                public void run() {
                    removeListeners();
                    frontBackPreviewToggle.setChecked(isBack);
                    applyListeners();
                }
            });
    }

    @Override
    public void onChange(boolean isChecked) {
        if (canChangeUI() && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                colorsContainer.getLocationInWindow(colorsViewLocation);
                if (isChecked && !activity.isMenuOpened() && cameraPreviewToggle.isChecked()) {
                    rectangle = new Rect();
                    window = getActivity().getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
                    colorsContainer.getLocationInWindow(colorsViewLocation);
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).showPreview(colorsViewLocation[0], colorsViewLocation[1] - rectangle.top, colorsContainer.getWidth(), colorsContainer.getHeight());
                } else if (!isChecked || activity.isMenuOpened()) {
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).hidePreview();
                    if (!isChecked && !activity.isMenuOpened()) {
                        removeListeners();
                        cameraPreviewToggle.setChecked(false);
                        applyListeners();
                    }
                }
            }
        }
    }


    @Override
    public void onMenuClosed() {
        if (canChangeUI() && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            rectangle = new Rect();
            window = getActivity().getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
            colorsContainer.getLocationInWindow(colorsViewLocation);
            ((ColorDetectionShield) getApplication().getRunningShields().get(
                    getControllerTag())).invalidatePreview(colorsViewLocation[0], colorsViewLocation[1] - rectangle.top);
            if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                colorsContainer.getLocationInWindow(colorsViewLocation);
                if (((CheckBox) activity.findViewById(R.id.isMenuOpening)).isChecked() && cameraPreviewToggle.isChecked()) {
                    uiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((ColorDetectionShield) getApplication().getRunningShields().get(
                                    getControllerTag())).showPreview(colorsViewLocation[0], colorsViewLocation[1] - rectangle.top, colorsContainer.getWidth(), colorsContainer.getHeight());
                        }
                    }, 100);
                } else
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).hidePreview();
            }
        }
    }
}
