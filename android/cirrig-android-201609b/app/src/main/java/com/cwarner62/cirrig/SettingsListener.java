package com.cwarner62.cirrig;

//interface for a listener to apply changes to settings as entered in NumericSettingsDialog
public interface SettingsListener {
  public void onOkClick(String value);
  public void onCancelClick();
}
