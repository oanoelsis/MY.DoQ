using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.IO;

namespace MY.DoQ_WPF
{
    /// <summary>
    /// MainWindow.xaml에 대한 상호 작용 논리
    /// </summary>
    public partial class MainWindow : Window
    {
        #region constant
        TcpClient client = null;
        NetworkStream networkStream;
        StreamWriter writer;
        StreamReader reader;
        /*
        List<string> result_1st_query;
        List<string> result_2nd_query;
        List<string> result_3rd_query;
        List<string> result_4th_query;
         */
        JObject json;
        int suggestion_num = 8;
        public listboxitem_document document { get; set; }

        #endregion

        #region setup and connect
        public MainWindow()
        {
            InitializeComponent();
            // lucene server connect
            try
            {
                client = new TcpClient();
                client.Connect("localhost", 8787);
                networkStream = client.GetStream();
                writer = new StreamWriter(networkStream);
                reader = new StreamReader(networkStream);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Connection error: " + ex.ToString(), "Alert", MessageBoxButton.OK);
            }
            MessageBox.Show("Connection Established!");

        }



        #endregion

        private void enter_search(object sender, KeyEventArgs e) // 텍스트 박스에서 엔터누르면 서치
        {
            if (e.Key == Key.Return)
            {
                #region send search query and get result
                string query_input = "m" + QueryBox.Text; // m 은 메인 서치임을 구분하기 위한 시그널.
                if (!String.IsNullOrEmpty(query_input))
                {
                    byte[] send_bytes = System.Text.Encoding.UTF8.GetBytes(query_input);
                    byte[] receive_bytes = new byte[4096];

                    try
                    {
                        networkStream.Write(send_bytes, 0, send_bytes.Length);
                        string result_text = "";
                        do
                        {
                            int numberOfBytesRead = networkStream.Read(receive_bytes, 0, receive_bytes.Length);
                            string temp_text = System.Text.Encoding.UTF8.GetString(receive_bytes);
                            result_text += temp_text;
                        }
                        while (networkStream.DataAvailable);
                #endregion

                        #region 메인 쿼리 결과 텍스트파일에서 받아오고 파싱
                        string path = "C:\\Users\\user\\Documents\\MY.DoQ\\out.txt";
                        string textValue = "";
                        textValue = System.IO.File.ReadAllText(path, Encoding.Default);
                        json = (JObject)JsonConvert.DeserializeObject(textValue);
                        // json으로 리턴된 result를 각 항목에 따라 분류
                        string suggestion_keyword = json["main_query"]["suggestion_keyword"].ToString();
                        suggestion_keyword = suggestion_keyword.Substring(1, suggestion_keyword.Length - 2);
                        List<string> suggestion_keyword_list = suggestion_keyword.Split(new string[] { ", " }, StringSplitOptions.None).ToList();
                        suggestion_keyword = "";
                        for (int i = 0; i < suggestion_num; i++) {
                            suggestion_keyword += suggestion_keyword_list[i] + "    ";
                        }
                        suggestion_textbox.Text = suggestion_keyword;
                        // [ 와 ] 도 벗겨내어야 하고, //\ 와 같은 주소지정자 들도 벗겨내어야 함.
                        string result_main_query_string = json["main_query"]["result"].ToString();
                        result_main_query_string = result_main_query_string.Substring(1, result_main_query_string.Length - 2);
                        List<string> result_main_query = result_main_query_string.Split(new string[] { ", " }, StringSplitOptions.None).ToList();
                        #endregion

                        #region main query result listbox UI
                        mainquerylist.Items.Clear();
                        subquerylist.Items.Clear();
                        int idx = 0;
                        foreach (string file in result_main_query)
                        {
                            int start = file.LastIndexOf('\\');
                            string file_add = file;
                            string name = file.Substring(start + 1, file.Length - start - 1);
                            start = file.LastIndexOf('.');
                            string str_format = file.Substring(start + 1, file.Length - start - 1);
                            Format format = new Format();
                            favorite favorite = new favorite();
                            format = str2format(str_format);
                            if (idx < 3) // 임시로 상위 3개문서는 즐겨찾기를 해놓아서 위에 나오는 것 처럼 설정.
                            {
                                favorite = favorite.YES;
                            }
                            else
                            {
                                favorite = favorite.NO;
                            }

                            document = new listboxitem_document(name, file, format, favorite);
                            mainquerylist.Items.Add(document);

                            idx++;
                        }
                        #endregion

                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show("Data send error" + ex);
                    }

                    // main window resize 
                    Application.Current.MainWindow.Height = 667;
                }
            }
        }

        #region 문서 분류를 위한 유틸 함수 들
        private Format str2format(string str)
        {
            Format format = new Format();
            switch (str)
            {
                case "hwp":
                    format = Format.hwp;
                    break;
                case "pdf":
                    format = Format.pdf;
                    break;
                case "ppt":
                    format = Format.ppt;
                    break;
                case "pptx":
                    format = Format.ppt;
                    break;
                case "doc":
                    format = Format.word;
                    break;
                case "docx":
                    format = Format.word;
                    break;
                default:
                    format = Format.pdf;
                    //MessageBox.Show("unknown document type: " + str);
                    break;
            }
            return format;
        }

        private string filesize2str(long size)
        {
            string sLen = size.ToString();
            if (size >= (1 << 30))
                sLen = string.Format("{0}Gb", size >> 30);
            else
                if (size >= (1 << 20))
                    sLen = string.Format("{0}Mb", size >> 20);
                else
                    if (size >= (1 << 10))
                        sLen = string.Format("{0}Kb", size >> 10);
            return sLen;
        }
        #endregion
        #region borderless 창을 만들기 위한 코드

        private void rectangle1_MouseDown(object sender, MouseButtonEventArgs e)
        {
            this.DragMove();
        }

        #endregion
        #region minimize icon and close icon
        private void close(object sender, MouseButtonEventArgs e)
        {
            QueryBox.Clear();
            return;
            //this.Close();
        }

        private void minimize(object sender, MouseButtonEventArgs e)
        {
            return;
            //this.WindowState = WindowState.Minimized;
        }
        #endregion
        #region 메인 쿼리 리스트에서 문서를 클릭 햇을 때 발생하는 함수, 문서 정보를 가져오고 서브쿼리를 날려와서 서브쿼리 리스트에 나타내야함.

        private void mainlist_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            // listbox.clear() 가 selectionchanged event를 발생시키기 떄문에 그걸 ㅇ외 처리해줌
            if (mainquerylist.Items.Count == 0)
                return;


            listboxitem_document doc = (listboxitem_document)e.AddedItems[0];

            // 문서 셀렉 할떄마다 문서 정보 채워넣기.
            try
            {
                FileInfo fileinfo = new FileInfo(doc.file);
                //MessageBox.Show(fileinfo.ToString());
                file_size_textbox.Text = filesize2str(fileinfo.Length);
                file_repair_date_textbox.Text = fileinfo.LastAccessTime.ToString();
                //file_creator_textbox.Text = System.IO.File.GetAccessControl(doc.file).GetOwner(typeof(System.Security.Principal.NTAccount)).ToString();
                file_creator_textbox.Text = fileinfo.DirectoryName;
            }
            catch (FileNotFoundException) { return; }
            // 루씬으로 서브 쿼리 날려서 서브 리스트에 채워 넣기
            subquerylist.Items.Clear();
            string query_input = "s" + doc.name; // x는 서브 서치라는걸 구분 하기 위한 시그널.


            #region 쿼리 보내고 쿼리 완료되었는지 데이터 받는걸로 확인하는 부분.
            if (!String.IsNullOrEmpty(query_input))
            {
                byte[] send_bytes = System.Text.Encoding.UTF8.GetBytes(query_input);
                byte[] receive_bytes = new byte[4096];

                try
                {
                    networkStream.Write(send_bytes, 0, send_bytes.Length);
                    string result_text = "";
                    do
                    {
                        int numberOfBytesRead = networkStream.Read(receive_bytes, 0, receive_bytes.Length);
                        string temp_text = System.Text.Encoding.UTF8.GetString(receive_bytes);
                        result_text += temp_text;
                    }
                    while (networkStream.DataAvailable);
                }
                catch (Exception ex)
                {
                    MessageBox.Show("Data send error" + ex);
                }
            }

            #endregion
            string path = "C:\\Users\\user\\Documents\\MY.DoQ\\out.txt";
            string textValue = "";
            textValue = System.IO.File.ReadAllText(path, Encoding.Default);
            json = (JObject)JsonConvert.DeserializeObject(textValue);
            // json으로 리턴된 result를 각 항목에 따라 분류
            string suggestion_keyword = json["sub_query"]["suggestion_keyword"].ToString();
            suggestion_keyword = suggestion_keyword.Substring(1, suggestion_keyword.Length - 2);
            List<string> suggestion_keyword_list = suggestion_keyword.Split(new string[] { ", " }, StringSplitOptions.None).ToList();
            suggestion_keyword = "";
            for (int i = 0; i < suggestion_num; i++)
            {
                suggestion_keyword += suggestion_keyword_list[i] + "    ";
            }
            suggestion_textbox.Text = suggestion_keyword;
            // [ 와 ] 도 벗겨내어야 하고, //\ 와 같은 주소지정자 들도 벗겨내어야 함.
            string result_sub_query_string = json["sub_query"]["result"].ToString();
            result_sub_query_string = result_sub_query_string.Substring(1, result_sub_query_string.Length - 2);
            List<string> result_sub_query = result_sub_query_string.Split(new string[] { ", " }, StringSplitOptions.None).ToList();

            //mainquerylist.Items.Clear();
            //subquerylist.Items.Clear();
            int idx = 0;
            foreach (string file in result_sub_query)
            {
                int start = file.LastIndexOf('\\');
                string file_add = file;
                string name = file.Substring(start + 1, file.Length - start - 1);
                start = file.LastIndexOf('.');
                string str_format = file.Substring(start + 1, file.Length - start - 1);
                Format format = new Format();
                favorite favorite = new favorite();
                format = str2format(str_format);
                if (idx < 2) // 임시로 상위 2개문서는 즐겨찾기를 해놓아서 위에 나오는 것 처럼 설정.
                {
                    favorite = favorite.YES;
                }
                else
                {
                    favorite = favorite.NO;
                }

                document = new listboxitem_document(name, file, format, favorite);
                subquerylist.Items.Add(document);

                idx++;
            }

        }
        #endregion
        #region 서브 쿼리 리스트에서 문서를 클릭 했을 떄 발생하는 함수, 문서정보를 가져와서 표시함.
        private void sublist_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (subquerylist.Items.Count == 0)
                return;

            listboxitem_document doc = (listboxitem_document)e.AddedItems[0];
            try
            {
                // 문서 셀렉 할떄마다 문서 정보 채워넣기.
                FileInfo fileinfo = new FileInfo(doc.file);
                file_size_textbox.Text = filesize2str(fileinfo.Length);
                file_repair_date_textbox.Text = fileinfo.LastAccessTime.ToString();
                //file_creator_textbox.Text = System.IO.File.GetAccessControl(doc.file).GetOwner(typeof(System.Security.Principal.NTAccount)).ToString();
                file_creator_textbox.Text = fileinfo.FullName;
            }
            catch(Exception){return;}
        }
        #endregion
        #region 메인 쿼리 리스트에서 문서 더블 클릭해서 파일 열어주는 함수.
        private void mainlist_fileopen(object sender, MouseButtonEventArgs e)
        {
            listboxitem_document doc = (listboxitem_document)mainquerylist.SelectedItem;
            Process proc = new Process();
            proc.StartInfo.FileName = doc.file;
            proc.StartInfo.UseShellExecute = true;
            proc.Start();
            //MessageBox.Show(doc.name);
        }
        #endregion
        #region 서브 쿼리 리스트에서 문서 더블 클릭해서 파일 열어주는 함수.
        private void sublist_fileopen(object sender, MouseButtonEventArgs e)
        {
            listboxitem_document doc = (listboxitem_document)subquerylist.SelectedItem;
            Process proc = new Process();
            proc.StartInfo.FileName = doc.file;
            proc.StartInfo.UseShellExecute = true;
            proc.Start();
        }
        #endregion

        private void xicon_visible(object sender, TextChangedEventArgs e)
        {
            xicon.Visibility = System.Windows.Visibility.Visible;
        }

        private void mainlist_enter_openfile(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Return)
            {
                listboxitem_document doc = (listboxitem_document)mainquerylist.SelectedItem;
                Process proc = new Process();
                proc.StartInfo.FileName = doc.file;
                proc.StartInfo.UseShellExecute = true;
                proc.Start();
                return;
            }
            else if(e.Key == Key.Right)
            {
                if (subquerylist.SelectedIndex == -1) subquerylist.SelectedIndex = 0;
                Keyboard.Focus(subquerylist);
                return;
            }

            return;
        }

        private void sublist_enter_openfile(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Return)
            {
                listboxitem_document doc = (listboxitem_document)subquerylist.SelectedItem;
                Process proc = new Process();
                proc.StartInfo.FileName = doc.file;
                proc.StartInfo.UseShellExecute = true;
                proc.Start();
                return;
            }
            else if (e.Key == Key.Left)
            {
                if (mainquerylist.SelectedIndex == -1) mainquerylist.SelectedIndex = 0;
                Keyboard.Focus(mainquerylist);
            }
            return;
        }




    }
}
