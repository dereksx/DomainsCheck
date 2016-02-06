package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

public class MainView extends JFrame implements ActionListener, MouseListener{
	/**
	 * 自动化批量检测域名是否已注册
	 * @author zifangsky
	 * @blog http://www.zifangsky.cn
	 * @date 2015-12-30
	 * @version v1.0.0
	 */
	private static final long serialVersionUID = 1L;
	private GridBagLayout gridbag;
	private GridBagConstraints constraints;
	private JPanel mainJPanel,panel1,panel2,panel3,panel4,panel5,panel6,panel7;
	private JButton selectDic,beginCheck;  // 选择字典，开始检测
	private JLabel domainSuffix,custom,progress,result;  //域名后缀，自定义后缀，探测进度，结果
	private JFileChooser dicChooser;  //字典选择
	private JTextField customJTextField;  //自定义
	private JScrollPane progressPane,resultPane;  //进度面板和结果面板
	private JTextArea progressJtJTextArea,resultJTextArea;  //同上
	private JCheckBox[] suffixCheckBoxs = new JCheckBox[15];  //域名后缀多项选择框
	
	private JMenuBar jMenuBar;
	private JMenu help;
	private JMenuItem author,contact,version,readme;
	private JPopupMenu outPutData;  //导出数据
	private JMenuItem availableDomains,timeOutDomains,allDomains;  //可用域名，超时域名，全部域名
	
	private Font menuFont = new Font("宋体", Font.LAYOUT_NO_LIMIT_CONTEXT, 14);  //菜单字体
	private Font contentFont = new Font("宋体", Font.LAYOUT_NO_LIMIT_CONTEXT, 16);  //正文字体
	
	private String dicName = "",currentDomain = "";  //字典名字，当前检测域名
	private DomainsCheckThread myThread = null;  //查询线程
	private Runnable progressRunnable,resultRunnable,timedOutRunnable,endRunnable;  //更新页面线程
	
	public MainView(){
		super("自动化域名检测");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setPreferredSize(new Dimension(1000, 650));
		int frameWidth = this.getPreferredSize().width;  //界面宽度
		int frameHeight = this.getPreferredSize().height;  //界面高度
		setSize(frameWidth,frameHeight);
		setLocation((screenSize.width - frameWidth) / 2,(screenSize.height - frameHeight) / 2);
		
		//初始化
		mainJPanel = new JPanel();
		panel1 = new JPanel();
		panel2 = new JPanel();
		panel3 = new JPanel();
		panel4 = new JPanel();
		panel5 = new JPanel();
		panel6 = new JPanel();
		panel7 = new JPanel();
		selectDic = new JButton("导入字典文件");
		beginCheck = new JButton("开始检测");
		domainSuffix = new JLabel("域名后缀：");
		custom = new JLabel("自定义（以英文空格分割，如：.club .win）：");
		progress = new JLabel("探测进度：");
		result = new JLabel("探测结果：");
		customJTextField = new JTextField("",70);
		progressPane = new JScrollPane();
		resultPane = new JScrollPane();
		progressJtJTextArea = new JTextArea(20, 20);
		resultJTextArea = new JTextArea(20, 20);
		suffixCheckBoxs[0] = new JCheckBox(".com");
		suffixCheckBoxs[1] = new JCheckBox(".cn");
		suffixCheckBoxs[2] = new JCheckBox(".com.cn");
		suffixCheckBoxs[3] = new JCheckBox(".org");
		suffixCheckBoxs[4] = new JCheckBox(".net");
		suffixCheckBoxs[5] = new JCheckBox(".me");
		suffixCheckBoxs[6] = new JCheckBox(".cc");
		suffixCheckBoxs[7] = new JCheckBox(".xyz");
		suffixCheckBoxs[8] = new JCheckBox(".top");
		suffixCheckBoxs[9] = new JCheckBox(".xin");
		suffixCheckBoxs[10] = new JCheckBox(".biz");
		suffixCheckBoxs[11] = new JCheckBox(".tv");
		suffixCheckBoxs[12] = new JCheckBox(".ren");
		suffixCheckBoxs[13] = new JCheckBox(".wang");
		suffixCheckBoxs[14] = new JCheckBox(".link");
		suffixCheckBoxs[0].setSelected(true);  //第一个默认选中
		
		//布局
		gridbag = new GridBagLayout();
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		mainJPanel.setLayout(gridbag);
		
		constraints.gridwidth = 0;  //该方法是设置组件水平所占用的格子数，如果为0，就说明该组件是该行的最后一个
		constraints.gridheight = 1;
		constraints.weightx = 1;  //该方法设置组件水平的拉伸幅度，如果为0就说明不拉伸，不为0就随着窗口增大进行拉伸，0到1之间
		constraints.weighty = 0;  //该方法设置组件垂直的拉伸幅度，如果为0就说明不拉伸，不为0就随着窗口增大进行拉伸，0到1之间
		gridbag.setConstraints(panel1, constraints);
		mainJPanel.add(panel1);
		
		constraints.gridheight = 2;
		gridbag.setConstraints(panel2, constraints);
		mainJPanel.add(panel2);
		
		constraints.gridheight = 1;
		gridbag.setConstraints(panel3, constraints);
		mainJPanel.add(panel3);
		
		gridbag.setConstraints(panel4, constraints);
		mainJPanel.add(panel4);
		
		constraints.weightx = 1;
		constraints.weighty = 1;
		gridbag.setConstraints(panel5, constraints);
		mainJPanel.add(panel5);
		
		panel1.setLayout(new FlowLayout(FlowLayout.CENTER));
		selectDic.setFont(contentFont);
		panel1.add(selectDic);
		panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		domainSuffix.setFont(contentFont);
		panel2.add(domainSuffix);
		for(int i=0;i<15;i++){
			suffixCheckBoxs[i].setFont(contentFont);
			panel2.add(suffixCheckBoxs[i]);
		}
		panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
		custom.setFont(contentFont);
		panel3.add(custom);
		customJTextField.setFont(contentFont);
		panel3.add(customJTextField);
		panel4.setLayout(new FlowLayout(FlowLayout.CENTER));
		beginCheck.setFont(contentFont);
		panel4.add(beginCheck);
		panel5.setLayout(new GridLayout(1, 2));
		panel5.add(panel6);
		panel5.add(panel7);
		panel6.setLayout(new BorderLayout());
		progress.setFont(contentFont);
		progress.setHorizontalAlignment(JLabel.CENTER);
		panel6.add(progress,BorderLayout.NORTH);
		panel6.add(progressPane,BorderLayout.CENTER);
		progressJtJTextArea.setFont(contentFont);
		progressPane.setViewportView(progressJtJTextArea);
		progressJtJTextArea.setEditable(false);
		progressJtJTextArea.setLineWrap(true);
		progressJtJTextArea.setWrapStyleWord(true);
		panel7.setLayout(new BorderLayout());
		result.setFont(contentFont);
		result.setHorizontalAlignment(JLabel.CENTER);
		panel7.add(result,BorderLayout.NORTH);
		panel7.add(resultPane,BorderLayout.CENTER);
		resultJTextArea.setFont(contentFont);
		resultPane.setViewportView(resultJTextArea);
		resultJTextArea.setEditable(false);
		resultJTextArea.setLineWrap(true);
		resultJTextArea.setWrapStyleWord(true);
		
		//菜单
		jMenuBar = new JMenuBar();
		help = new JMenu("帮助");
		author = new JMenuItem("作者");
		contact = new JMenuItem("联系方式");
		version = new JMenuItem("版本号");
		readme = new JMenuItem("说明");
		help.setFont(menuFont);
		jMenuBar.add(help);
		author.setFont(menuFont);
		help.add(author);
		contact.setFont(menuFont);
		help.add(contact);
		version.setFont(menuFont);
		help.add(version);
		readme.setFont(menuFont);
		help.add(readme);
		
		//鼠标右键导出菜单
		outPutData = new JPopupMenu();
		availableDomains = new JMenuItem("导出可注册域名");
		availableDomains.setFont(menuFont);
		outPutData.add(availableDomains);
		timeOutDomains = new JMenuItem("导出超时域名");
		timeOutDomains.setFont(menuFont);
		outPutData.add(timeOutDomains);
		allDomains = new JMenuItem("导出所有结果域名");
		allDomains.setFont(menuFont);
		outPutData.add(allDomains);
		
		add(mainJPanel);
		setJMenuBar(jMenuBar);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//点击事件
		selectDic.addActionListener(this);
		beginCheck.addActionListener(this);
		author.addActionListener(this);
		contact.addActionListener(this);
		version.addActionListener(this);
		readme.addActionListener(this);
		availableDomains.addActionListener(this);
		timeOutDomains.addActionListener(this);
		allDomains.addActionListener(this);
		
		//鼠标事件
		resultJTextArea.addMouseListener(this);
		
		//组件更新线程
		progressRunnable = new Runnable() {
			public void run() {
				progressJtJTextArea.setEditable(true);
				progressJtJTextArea.append("正在检测：" + currentDomain + "\n");	
				progressJtJTextArea.setEditable(false);
				//设置显示最新内容
				progressJtJTextArea.selectAll();
				progressJtJTextArea.setCaretPosition(progressJtJTextArea.getSelectionEnd());
			}
		};
		resultRunnable = new Runnable() {
			public void run() {
				resultJTextArea.setEditable(true);
				resultJTextArea.append(currentDomain + "    可以注册\n");
				resultJTextArea.setEditable(false);
				resultJTextArea.selectAll();
				resultJTextArea.setCaretPosition(resultJTextArea.getSelectionEnd());
			}
		};
		timedOutRunnable = new Runnable() {
			public void run() {
				resultJTextArea.setEditable(true);
				resultJTextArea.append(currentDomain + "    超时\n");
				resultJTextArea.setEditable(false);		
				resultJTextArea.selectAll();
				resultJTextArea.setCaretPosition(resultJTextArea.getSelectionEnd());
			}
		};
		endRunnable = new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, "任务全部执行完毕","提示：",JOptionPane.INFORMATION_MESSAGE);		
			}
		};
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainView();
			}
		});
	}

	/**
	 * 处理点击事件
	 * */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == selectDic){
			dicChooser = new JFileChooser();
			dicChooser.setFont(contentFont);
			FileSystemView fileSystemView = FileSystemView.getFileSystemView();
			dicChooser.setCurrentDirectory(fileSystemView.getHomeDirectory());
			dicChooser.setDialogTitle("请选择字典文件：");
			dicChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int choose = dicChooser.showDialog(null, "打开");
			if(choose == JFileChooser.APPROVE_OPTION){
				File newFile = dicChooser.getSelectedFile();
				if(newFile != null)
					dicName = newFile.getAbsolutePath();  //字典文件名			
			}
		}
		else if(e.getSource() == beginCheck){
			if(myThread != null)
				return ;
			progressJtJTextArea.setText("");
			resultJTextArea.setText("");
			if(dicName == null || "".equals(dicName)){
				JOptionPane.showMessageDialog(this, "请先选择一个TXT格式的字典文件！！！","警告：",JOptionPane.ERROR_MESSAGE);
				return ;
			}
			//获取选择的后缀
			Set<String> suffixSet = new LinkedHashSet<String>();
			for(int i=0;i<15;i++){
				if(suffixCheckBoxs[i].isSelected())
					suffixSet.add(suffixCheckBoxs[i].getText());
			}
			String customSuffix = customJTextField.getText();  //获取自定义域名后缀
			if(!"".equals(customSuffix)){
				String[] customs = customSuffix.split(" ");
				for(String tString : customs){
					if(!"".equals(tString))
						suffixSet.add(tString);
				}
			}
			
			if(suffixSet.isEmpty()){
				JOptionPane.showMessageDialog(this, "请先选择一个或多个的域名后缀！！！","警告：",JOptionPane.ERROR_MESSAGE);				
				return;
			}
			//开启新的线程，读字典文件，并且查询
			myThread = new DomainsCheckThread(suffixSet);
			Thread thread = new Thread(myThread);
			thread.start();
		}
		else if(e.getSource() == author){
			JOptionPane.showMessageDialog(this, "zifangsky","作者：",JOptionPane.INFORMATION_MESSAGE);
		}
		else if(e.getSource() == contact){
			JOptionPane.showMessageDialog(this, "邮箱：admin@zifangsky.cn\n" +
					"博客：http://www.zifangsky.cn","联系方式：",JOptionPane.INFORMATION_MESSAGE);
		}
		else if(e.getSource() == version){
			JOptionPane.showMessageDialog(this, "v1.0.0","版本号：",JOptionPane.INFORMATION_MESSAGE);
		}
		else if(e.getSource() == readme){
			JOptionPane.showMessageDialog(this, "本程序是判断域名是否可以注册的自动化查询工具，使用的是万网的接口。\n" +
					"使用方式简单，只需要导入相关字典文件即可。\n" +
					"目前还不太完善，仅仅只是单线程的。等过段时间有空了，我或许会将之升级成多线程版。\n" +
					"另外，源码已经开放，需要源码进行自行研究的可自行移步到我的个人博客网站。","说明：",JOptionPane.INFORMATION_MESSAGE);
		}
		else if(e.getSource() == availableDomains){
			Pattern pattern = Pattern.compile("(.*?)\\s*可以注册");
			resultOutPut(pattern,1);
			
		}
		else if(e.getSource() == timeOutDomains){
			Pattern pattern = Pattern.compile("([^\\s]*?)    超时");
			resultOutPut(pattern,2);
		}
		else if(e.getSource() == allDomains){
			Pattern pattern = Pattern.compile("([^\\s]*?)    ");
			resultOutPut(pattern,3);
		}
		
	}
	
	/**
	 * 查询到的结果进行导出，根据不同的正则表达式分为：导出可注册的，导出超时的，导出所有的
	 * @param pattern 导出操作的正则表达式
	 * @param state 状态：可注册-->1;超时-->2;所有-->3
	 * @return null
	 * */
	private void resultOutPut(Pattern pattern,int state){
		String[] data = resultJTextArea.getText().split("\n");
		
		Date date = new Date();
		Format format = new SimpleDateFormat("HHmmss");
		String fileName = "";
		if(state == 1)
			fileName = "可注册域名导出列表" + format.format(date) + ".txt";
		else if(state == 2)
			fileName = "超时域名导出列表" + format.format(date) + ".txt";
		else if(state == 3)
			fileName = "所有结果域名导出列表" + format.format(date) + ".txt";
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));
			Matcher matcher = null;
			for(String temp : data){
				matcher = pattern.matcher(temp);
				if(matcher.find()){
					writer.write(matcher.group(1));
					writer.newLine();
					writer.flush();
				}				
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 查询线程，用的万网的接口
	 * */
	class DomainsCheckThread implements Runnable{
		private Set<String> suffixSet = new LinkedHashSet<String>();
		
		public DomainsCheckThread(Set<String> suffixSet) {
			this.suffixSet = suffixSet;
		}

		public void run() {
			//读字典
			try {
				BufferedReader reader = new BufferedReader(new FileReader(new File(dicName)));
				String line = "";			
				while((line = reader.readLine()) != null){				
					if(!"".equals(line.trim())){
						Iterator<String> iterator = suffixSet.iterator();
						while(iterator.hasNext()){
							currentDomain = line.trim() + iterator.next();
							SwingUtilities.invokeLater(progressRunnable);  //更新状态
							checkDomain(currentDomain);  //开始查询
							try {
								Thread.sleep(1000);  //单线程，并且每次查询完毕暂停1秒
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//任务结束，参数初始化
			dicName = "";
			currentDomain = "";
			myThread = null;
			SwingUtilities.invokeLater(endRunnable);  //结束通知
		}
		
		/**
		 * 对单个域名向万网的接口发起请求，检测注册情况
		 * @param domain 域名
		 * @return null
		 * */
		public void checkDomain(String domain){
			try {
				URL url = new URL("http://panda.www.net.cn/cgi-bin/check.cgi?area_domain=" + domain);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(3000);  //毫秒
				connection.setReadTimeout(3000);
				
				if(connection.getResponseCode() == 200){
					InputStream inputStream = new BufferedInputStream(connection.getInputStream());				
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line = "";
					String reg = "<original>(.*?)</original>";
					while((line = reader.readLine()) != null){
						if(line.matches(reg)){
							//211表示不可用
							String state = line.substring(10, 13);
							if(!"211".equals(state)){
								//该域名未被使用，更新状态
								SwingUtilities.invokeLater(resultRunnable);
							}
												
						}
					}
					reader.close();
					inputStream.close();
				}
				connection.disconnect();
			}  catch (IOException e) {
				//超时，更新状态
				SwingUtilities.invokeLater(timedOutRunnable);
			}		
		}
		
	}

	/**
	 * 鼠标点击
	 * */
	public void mouseClicked(MouseEvent e) {
		//鼠标右键点击探测结果面板时，弹出数据导出菜单
		if(e.getButton() ==MouseEvent.BUTTON3){
			outPutData.show(resultJTextArea, e.getX(), e.getY());		
		}
	}

	public void mousePressed(MouseEvent e) {
		
	}

	public void mouseReleased(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {
		
	}

	public void mouseExited(MouseEvent e) {
		
	}

}
