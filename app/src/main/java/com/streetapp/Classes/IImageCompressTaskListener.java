package com.streetapp.Classes;

import java.io.File;
import java.util.List;

public interface IImageCompressTaskListener {

	public void onComplete(List<File> compressed);
	public void onError(Throwable error);

}
