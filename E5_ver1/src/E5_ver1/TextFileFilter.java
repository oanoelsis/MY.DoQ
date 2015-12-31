package E5_ver1;

import java.io.File;
import java.io.FileFilter;

public class TextFileFilter implements FileFilter{
	
	@Override
	public boolean accept(File pathname) {
		// txt, doc, docx, pdf, ppt, pptx, hwp support, "should add hwp and image file and mp3 file".
		return (pathname.getName().toLowerCase().endsWith(".txt")||pathname.getName().toLowerCase().endsWith(".doc")||pathname.getName().toLowerCase().endsWith(".docx")||pathname.getName().toLowerCase().endsWith(".pdf")||pathname.getName().toLowerCase().endsWith(".ppt")||pathname.getName().toLowerCase().endsWith(".pptx")||pathname.getName().toLowerCase().endsWith(".hwp"));
	}
	
}
