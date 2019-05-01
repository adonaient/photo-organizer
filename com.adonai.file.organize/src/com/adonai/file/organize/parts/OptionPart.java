
package com.adonai.file.organize.parts;

import javax.inject.Inject;

import javax.annotation.PostConstruct;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.SWTResourceManager;

public class OptionPart {
	private String OLD_DIR_TOP;// 원본 파일 폴더위치
	private String NEW_DIR_TOP;// 정리한 파일 폴더위치
	private int fileCtrl = 0;// 파일복사 및 이동여부(0: 없음, 1: 복사, 2: 이동)
	private boolean isXlsxInfoBuild = true;// 엑셀파일생성여부

	public ProgressBar progressBar;
	public ProgressBar progressBar_folder;
	public Table table;
	private Text txtOrignal;
	private Text txtNew;
//	public Label lblMessage;
	public Button btnExecute;

	@Inject
	public OptionPart() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		Group group = new Group(parent, SWT.NONE);
		group.setFont(SWTResourceManager.getFont("맑은 고딕", 10, SWT.BOLD));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		group.setText("폴더선택");
		group.setLayout(new GridLayout(1, false));

		Composite composite_3 = new Composite(group, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite_3.setLayout(new GridLayout(3, false));

		Label lblNewLabel = new Label(composite_3, SWT.NONE);
		lblNewLabel.setText("원본폴더");

		txtOrignal = new Text(composite_3, SWT.BORDER);
		txtOrignal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnOrginalFolder = new Button(composite_3, SWT.NONE);
		GridData gd_btnOrginalFolder = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnOrginalFolder.widthHint = 50;
		btnOrginalFolder.setLayoutData(gd_btnOrginalFolder);
		btnOrginalFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = new DirectoryDialog(parent.getShell(), SWT.OPEN).open();
				if (path != null) {
					txtOrignal.setText(path);
				}
			}
		});
		btnOrginalFolder.setText("...");

		Label label = new Label(composite_3, SWT.NONE);
		label.setText("대상폴더");

		txtNew = new Text(composite_3, SWT.BORDER);
		txtNew.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnNewButton = new Button(composite_3, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton.widthHint = 50;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = new DirectoryDialog(parent.getShell(), SWT.OPEN).open();
				if (path != null) {
					txtNew.setText(path);
				}
			}
		});
		btnNewButton.setText("...");

		Label lblNewLabel_3 = new Label(group, SWT.NONE);
		lblNewLabel_3.setForeground(SWTResourceManager.getColor(165, 42, 42));
		lblNewLabel_3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_3.setText("* 대상폴더 미선택시 '원본폴더_tmp' 폴더가 대상폴더로 자동지정됩니다.");

		Group group_1 = new Group(parent, SWT.NONE);
		group_1.setFont(SWTResourceManager.getFont("맑은 고딕", 10, SWT.BOLD));
		group_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		group_1.setText("옵션");
		group_1.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(group_1, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		Button btnRadioButton_2 = new Button(composite, SWT.RADIO);
		btnRadioButton_2.setSelection(true);
		btnRadioButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fileCtrl = 0;
			}
		});
		btnRadioButton_2.setText("검수(이동및 복사안함)");

		Button btnRadioButton = new Button(composite, SWT.RADIO);
		btnRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fileCtrl = 1;
			}
		});
		btnRadioButton.setText("복사");

		Button btnRadioButton_1 = new Button(composite, SWT.RADIO);
		btnRadioButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fileCtrl = 2;
			}
		});
		btnRadioButton_1.setText("이동");

		Button btnCheckButton = new Button(group_1, SWT.CHECK);
		btnCheckButton.setSelection(true);
		btnCheckButton.setFont(SWTResourceManager.getFont("맑은 고딕", 9, SWT.ITALIC));
		btnCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isXlsxInfoBuild = btnCheckButton.getSelection();
			}
		});
		btnCheckButton.setText("엑셀파일생성유무");

		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(300);
		tblclmnNewColumn.setText("원본파일");

		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(300);
		tblclmnNewColumn_1.setText("대상파일");

		Composite composite_2 = new Composite(parent, SWT.NONE);
		composite_2.setToolTipText("");
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		composite_2.setLayout(new GridLayout(2, false));

		Label lblNewLabel_2 = new Label(composite_2, SWT.NONE);
		lblNewLabel_2.setFont(SWTResourceManager.getFont("맑은 고딕", 9, SWT.BOLD));
		lblNewLabel_2.setText("전체");

		progressBar_folder = new ProgressBar(composite_2, SWT.NONE);
		progressBar_folder.setToolTipText("전체 진행바입니다.");
		progressBar_folder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_1 = new Label(composite_2, SWT.NONE);
		lblNewLabel_1.setFont(SWTResourceManager.getFont("맑은 고딕", 9, SWT.BOLD));
		lblNewLabel_1.setText("파일");

		progressBar = new ProgressBar(composite_2, SWT.NONE);
		progressBar.setToolTipText("전체 진행바입니다.");
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		progressBar.setMinimum(0);

		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite_1.setLayout(new GridLayout(1, false));

//		lblMessage = new Label(composite_1, SWT.NONE);
//		lblMessage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//		lblMessage.setText("폴더와 옵션을 선택하시고 완료를 눌러주세요.");

		btnExecute = new Button(composite_1, SWT.NONE);
		btnExecute.setFont(SWTResourceManager.getFont("맑은 고딕", 10, SWT.BOLD));
		btnExecute.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		GridData gd_btnExecute = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnExecute.widthHint = 100;
		btnExecute.setLayoutData(gd_btnExecute);
		btnExecute.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				execute();
			}
		});
		btnExecute.setText("\uC2E4\uD589");
	}

	protected void execute() {
		String filepath = txtOrignal.getText();
		if (!filepath.equals("")) {
			OLD_DIR_TOP = filepath;
		} else {
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "경고", "원본폴더를 선택해주세요.");
			return;
		}
		filepath = txtNew.getText();
		if (!filepath.equals("")) {
			NEW_DIR_TOP = filepath;
		} else {
			if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "알림", "대상폴더를 선택해주세요.\n대상폴더 미선택시 '원본폴더_tmp' 폴더가 대상폴더로 자동지정됩니다.")) {
				NEW_DIR_TOP = OLD_DIR_TOP + "_tmp";
			} else {
				return;
			}
		}

		table.removeAll();

		OptionHandler optionHandler = new OptionHandler(OLD_DIR_TOP + "\\", NEW_DIR_TOP + "\\", fileCtrl, isXlsxInfoBuild, this);
		Thread thread = new Thread(optionHandler);
		thread.start();
	}
}
