package com.adonai.file.organize.parts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import com.adonai.tool.DateHandler;
import com.adonai.tool.FileHandler;
import com.adonai.tool.StringHandler;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class OptionHandler implements Runnable {
	//================================================================설정
	private String OLD_DIR_TOP;//원본 파일 폴더위치
	private String NEW_DIR_TOP;//정리한 파일 폴더위치
	private int fileCtrl = 1;//파일복사 및 이동여부(0: 없음, 1: 복사, 2: 이동)
	private int printLog = 2;//로그출력여부(0: 없음, 1: 폴더이동시, 2: 파일처리후, 3: 1, 2둘다)
	private boolean isXlsxInfoBuild = false;// 엑셀파일생성여부
	//================================================================설정
	private File FOLDER_ORIGINAL;
	private List<Object[]> orgList = new ArrayList<Object[]>();
	private int filecnt = 0;
	private int rowCnt = 0;
	private SXSSFSheet sheet;
	private AutoDetectParser parser = new AutoDetectParser();
	private BodyContentHandler bodyContentHandler = new BodyContentHandler();
	private ParseContext parseContext = new ParseContext();
	private Metadata metadata = new Metadata();
	private Date date = null;
	private Date dateEx = null;
	private String[] dateFormatAry;
	private File newFile = null;
	private StringBuffer extFolder;
	private StringBuffer fileFolder;
	private String[] initFolderAry;
	private String[] fileEntryAry;
	private StringBuffer cellValue;
	private com.drew.metadata.Metadata metadataEx;
	private final int[] TAG_ARY = {
			ExifIFD0Directory.TAG_DATETIME, 
			ExifSubIFDDirectory.TAG_DATETIME, 
	};
	private Directory[] dirAry = new Directory[TAG_ARY.length];
	/**
	 * 새로운 파일명이 들어갈곳
	 */
	private String[] filename = new String[2];
	
	private OptionPart optionPart;
	private TableItem tableItem;
	
	/**
	 * @param display 
	 * @wbp.parser.entryPoint
	 */
	public OptionHandler(String OLD_DIR_TOP, String NEW_DIR_TOP, int fileCtrl, boolean isXlsxInfoBuild, OptionPart optionPart) {
		this.OLD_DIR_TOP = OLD_DIR_TOP;
		this.FOLDER_ORIGINAL = new File(OLD_DIR_TOP);
		this.NEW_DIR_TOP = NEW_DIR_TOP;
		this.fileCtrl = fileCtrl;
		this.isXlsxInfoBuild = isXlsxInfoBuild;
		this.optionPart = optionPart;
	}
	
	/**
	 * //0:폴더갯수, 1: 파일갯수, 2: 사이즈
	 * @param directory
	 * @return
	 * @wbp.parser.entryPoint
	 */
	private int totFolderCnt = 0;
	private int totFolderCntAdd = 0;
	public void getFileInfo(String directory) {
		
		File dir = new File(directory);
		File[] list = dir.listFiles();

		if (list != null) {
			for (File file : list) { // 현재경로의 폴더, 파일 갯수 합산
				if (file.isDirectory()) {
					totFolderCnt++;
					getFileInfo(file.getAbsoluteFile().toString()); // 중단하고 발견한 폴더에서 재탐색
				} else if (file.isFile()) {
					
				}
			}
		}
	}

	@Override
	public void run() {
		exec();
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void exec() {
//		optionPart.lblMessage.getDisplay().syncExec(new Runnable() {
//			@Override
//			public void run() {
//				optionPart.lblMessage.setText("진행중...");
//			}
//		});
		optionPart.btnExecute.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				optionPart.btnExecute.setText("실행중...");
				optionPart.btnExecute.setEnabled(false);
			}
		});
		getFileInfo(OLD_DIR_TOP);
		optionPart.progressBar_folder.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				optionPart.progressBar_folder.setMaximum(totFolderCnt);
			}
		});
		
		try {
			File xlsFile = new File(NEW_DIR_TOP+DateHandler.getDate("yyyyMMdd HHmmss")+"_PhotoOrganizerInfo.xlsx");
			FileHandler.creatDir(NEW_DIR_TOP);
			FileOutputStream fileOut = null;
			
			if (isXlsxInfoBuild) 
				fileOut = new FileOutputStream(xlsFile);
			
			SXSSFWorkbook wb = new SXSSFWorkbook(); // Excel 2007 이상
			sheet = wb.createSheet("info");
			sheet.setZoom(80); //줌60%
			listFilesForFolder(FOLDER_ORIGINAL);
			if (fileOut != null) {
				wb.write(fileOut);
				wb.close();
				fileOut.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		optionPart.btnExecute.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				optionPart.btnExecute.setText("실행");
				optionPart.btnExecute.setEnabled(true);
			}
		});
	}

	int folderFileCnt = 0;
	/**
	 * @wbp.parser.entryPoint
	 */
	public void listFilesForFolder(final File folder) throws Exception {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				optionPart.progressBar_folder.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						optionPart.progressBar_folder.setSelection(++totFolderCntAdd);
					}
				});
				
				folderFileCnt = 0;
				listFilesForFolder(fileEntry);
				if (printLog == 1 || printLog == 3) {
					System.out.println(fileEntry.getPath()+" ("+filecnt+")");
				}
				filecnt = 0;
			} else {
				optionPart.progressBar.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						if (folderFileCnt == 0) {
							optionPart.progressBar.setMaximum(folder.list().length);
							optionPart.progressBar.setSelection(0);
						}
						optionPart.progressBar.setSelection(++folderFileCnt);
					}
				});
				
				cellValue = new StringBuffer();
				extFolder = new StringBuffer();
				fileFolder = new StringBuffer();
				
				optionPart.table.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						tableItem = new TableItem(optionPart.table, SWT.NONE, filecnt);
						tableItem.setText(0, fileEntry.getPath());
					}
				});
				
				cellValue.append(fileEntry + " -> ");
				
				filename = FileHandler.getFilenameAry(fileEntry.getName());
				filename[1] = filename[1].toLowerCase();
				
				int idx = getFileIdx(filename[1]);
				
				if (idx == -1) {
					
					optionPart.table.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							tableItem.setText(1, "제외대상");
						}
					});
					
					cellValue.append("제외대상");
				}else {
					initFolderAry = StringHandler.getTokenAry(FOLDER_ORIGINAL.getPath(), "\\");
					fileEntryAry = StringHandler.getTokenAry(fileEntry.getParent(), "\\");
					if (fileEntryAry.length > initFolderAry.length) {
						for (int i = initFolderAry.length; i < fileEntryAry.length; i++) {
							extFolder.append(fileEntryAry[i]+"/");
						}
					}
					
					String duplStr = "";
					if (idx == 1 || idx == 2) {
						date = null;
						if (fileEntry.length() != 0) {
							try {
								metadata = new Metadata();
								parser.parse(new FileInputStream(fileEntry), bodyContentHandler, metadata, parseContext);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							if (metadata != null) {
								String contentType = metadata.get("Content-Type");
//								System.out.println(contentType);
								int idxType = 0;
								if (contentType.contains("image")) {
									if (contentType.contains("photoshop")) {
										idxType = 1;
									}else {
										String datetimeOrgn = metadata.get("Date/Time Original");
										if (datetimeOrgn != null) {
											try {
												date = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(datetimeOrgn);
											} catch (Exception e) {
												e.printStackTrace();
												//java.text.ParseException: Unparseable date: "⑥"
											}
										}
									}
								}else if (contentType.contains("mp4")) {
									date = metadata.getDate(Property.get("date"));
								}
								
								if (idxType == 1) {
									metadataEx = ImageMetadataReader.readMetadata(fileEntry);
									dirAry[0] = metadataEx.getFirstDirectoryOfType(ExifIFD0Directory.class);
									dirAry[1] = metadataEx.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
									Calendar cal;
									out: for (int i = 0; i < dirAry.length; i++) {
										dateEx = null;
										if (dirAry[i] != null) {
											dateEx = dirAry[i].getDate(TAG_ARY[i]);
										}
										if (dateEx != null) {
											cal = Calendar.getInstance();
											cal.setTime(dateEx);
											cal.add(Calendar.HOUR, -9);//사진
											date = cal.getTime();
											break out;
										}
									}
								}
								
								if (date != null) {
									dateFormatAry = StringHandler.getTokenAry(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(date), "-");
									if (dateFormatAry.length >= 6 //날짜형식이 6개로 구분이되는지
											&& !dateFormatAry[0].startsWith("0") //년도가 0으로 시작하는지
											&& dateFormatAry[0].length() == 4 //년도가 4자리인지 체크
											&& date.before(new Date()) //오늘 이전인지 체크
											&& Integer.parseInt(dateFormatAry[0]) >= 1905//동영상 재셋팅날짜가 19040101이 있음
											) {
										fileFolder.append(dateFormatAry[0]+"/"+dateFormatAry[1]+"/"+dateFormatAry[2]+"/");//년/월/일 폴더 설정
										extFolder = new StringBuffer();
										filename[0] = dateFormatAry[0]+dateFormatAry[1]+dateFormatAry[2]+"_"+dateFormatAry[3]+dateFormatAry[4]+dateFormatAry[5];//파일명설정
										
										//중복파일처리
										boolean isDupl = false;
										for (Object[] fileinfoAry : orgList) {
											if (filename[0].equals(fileinfoAry[0]) 
													&& fileEntry.length() == (Long)fileinfoAry[1]
													&& filename[1].equals(fileinfoAry[2])
													) {
												duplStr = "dupl/";
												System.out.println("원본"+filename[0]+"("+fileEntry.length()+")"+" / 중복"+fileinfoAry[0]+"("+(Long)fileinfoAry[1]+")");
												isDupl = true;
												break;
											}
										}
										
										if (!isDupl) {
											Object[] fileinfoAry = new Object[3];
											fileinfoAry[0] = filename[0];
											fileinfoAry[1] = fileEntry.length();
											fileinfoAry[2] = filename[1];
											orgList.add(fileinfoAry);
										}
									}else {
										date = null;
									}
								}
							}
						}
						if (date == null) {
							if (idx == 1) {
								fileFolder.append("image/");
							}else if (idx == 2) {
								fileFolder.append("video/");
							}
						}
					}else if (idx == 3) {
						fileFolder.append("compressor/");
					}else if (idx == 4) {
						fileFolder.append("document/");
					}else if (idx == 5) {
						fileFolder.append("music/");
					}else {
						fileFolder.append("etc/");
					}
					newFile = new File(NEW_DIR_TOP+duplStr+fileFolder.toString()+extFolder+filename[0]+"."+filename[1]);
					
					int exitCnt = 0;
					while (newFile.exists()) {
						exitCnt++;
						newFile = new File(NEW_DIR_TOP+duplStr+fileFolder.toString()+extFolder+filename[0]+"_"+StringHandler.getLPAD(5, exitCnt)+"."+filename[1]);
					}
					
					if (fileCtrl == 1 || fileCtrl == 2) {
						FileHandler.creatDir(newFile.getParent());
						
						//이동, 복사중 선택
						if (fileCtrl == 1) {
							FileHandler.copyFile(fileEntry, newFile);//복사
						}else  {
							fileEntry.renameTo(newFile);//이동
						}
					}
					
					optionPart.table.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							tableItem.setText(1, newFile.getPath());
						}
					});
					cellValue.append(newFile);
				}
				sheet.createRow(rowCnt++).createCell(0).setCellValue(cellValue.toString());
				if (printLog == 2 || printLog == 3) {
					System.out.println(cellValue.toString());
				}
			}
		}
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	private int getFileIdx(String fileext) {
		String[] extImage = {
				"jpg", "gif", "png", "jpeg", "bmp", "raw", "orf", "psd", "ico", "tif"
		};
		String[] extCompressor = {
				//일반압축
				"zip", "zipx", "rar", 
				"7z", "alz", "egg", 
				"cab", "bh", "001", 
				"arj", "lha", "lzh", 
				"pma", "ace", "arc", 
				"aes", "zpaq", 
				//유닉스계열
				"tar", "gz", "tgz", 
				"bz", "bz2", "tbz", 
				"tbz2", "xz", "txz", 
				"lzma", "tlz", "lz", 
				"z",
				//자바
				"jar"
		};
		String[] extVideo = {
				"avi", "wmv", "wmp", "wm", "asf", 
				"mpg", "mpeg", "mpe", "m1v", "m2v", 
				"mpv2", "mp2v", "ts", "tp", "tpr", 
				"trp", 
				//DVD파일
//				"vob", "ifo", "bup", 
				"ogm", "ogv", 
				"mp4", "m4v", "m4p", "m4b", "3gp", 
				"3gpp", "3g2", "3gp2", "mkv", "rm", 
				"ram", "rmvb", "rpm", "flv", "swf", 
				"mov", "qt", "amr", "nsv", "dpg", 
				"m2ts", "m2t", "mts", "dvr-ms", "k3g", 
				"skm", "evo", "nsr", "amv", "divx", 
				"webm", "wtv", "f4v", "mxf", 
				
				"smi", "srt"
		};
		String[] extDoc = {
				"pptx", "xlsx", "docx", 
				"ppt", "xls", "doc", 
				//한글
				"hwp", "hml", 
				"txt", "pdf", 
//				"emf", "xlsm", "xla", 
//				"log", "xml", "chm"
		};
		String[] extMusic = {
				"mp3", "mpga", "ogg", "wav", "wma", 
				"ape", "flac", "m4a", "acc", "cda", "s3m", "mod", "asl", "m3u", "pls", "sab"
		};
		String[] except = {
				"db", "lnk"
		};
		for (String str : extImage) {
			if (str.equals(fileext)) return 1;
		}
		for (String str : extVideo) {
			if (str.equals(fileext)) return 2;
		}
		for (String str : extCompressor) {
			if (str.equals(fileext)) return 3;
		}
		for (String str : extDoc) {
			if (str.equals(fileext)) return 4;
		}
		for (String str : extMusic) {
			if (str.equals(fileext)) return 5;
		}
		for (String str : except) {
			if (str.equals(fileext)) return -1;
		}
		return 0;
	}
}
