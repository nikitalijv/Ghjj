package com.example.vwmqbcrc;
import android.app.*;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.content.SharedPreferences;
import java.net.URL;

import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class MainActivity extends Activity {
 EditText id,data,table; TextView result; Spinner seasons; TextView selectedCan,arrowCan; PopupWindow canPopup; ArrayList<Integer> canValues=new ArrayList<>(); LinearLayout page; FrameLayout frame; ImageView image; View veil; boolean selectionReady=false; int seasonIndex=0;
 int currentId(){return Integer.parseInt(id.getText().toString().trim().replaceFirst("(?i)^0x",""),16);}
 GradientDrawable translucentField(){
  GradientDrawable bg=new GradientDrawable();
  bg.setColor(Color.argb(179,255,255,255));
  bg.setCornerRadius(16f*getResources().getDisplayMetrics().density);
  bg.setStroke((int)(1.5f*getResources().getDisplayMetrics().density),Color.argb(220,155,184,205));
  return bg;
 }
 void styleInput(EditText field){
  GradientDrawable bg=new GradientDrawable();
  bg.setColor(Color.argb(179,255,255,255));
  bg.setCornerRadius(16f*getResources().getDisplayMetrics().density);
  bg.setStroke((int)(1.5f*getResources().getDisplayMetrics().density),Color.argb(220,155,184,205));
  field.setBackground(translucentField());
  field.setTextColor(Color.rgb(18,31,44));
  field.setHintTextColor(Color.rgb(90,105,117));
  field.setHighlightColor(Color.argb(85,39,139,195));
  field.setPadding(18,14,18,14);
  if(android.os.Build.VERSION.SDK_INT>=29)field.setTextCursorDrawable(new ColorDrawable(Color.rgb(12,95,151)));
 }
 void label(String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(16);t.setTextColor(Color.WHITE);t.setPadding(0,20,0,3);page.addView(t);}
 Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);page.addView(b);b.setOnClickListener(v->{try{r.run();}catch(Exception e){result.setText("Ошибка: "+e.getMessage());}});return b;}
 void save(){StringBuilder b=new StringBuilder();for(Map.Entry<Integer,int[]> e:MQBCrc.customTables().entrySet())b.append(Integer.toHexString(e.getKey())).append('=').append(MQBCrc.tableHex(e.getValue())).append('\n');getPreferences(0).edit().putString("tables",b.toString()).apply();}
 void load(){MQBCrc.clearCustom();for(String l:getPreferences(0).getString("tables","").split("\\n")){String[] p=l.split("=",2);if(p.length==2)try{MQBCrc.put(Integer.parseInt(p[0],16),MQBCrc.parseTable(p[1]));}catch(Exception ignored){}}}
 void refresh(){
  TreeSet<Integer> all=new TreeSet<>(MQBCrc.builtinTables().keySet());
  all.addAll(MQBCrc.customTables().keySet());
  canValues=new ArrayList<>(all);
  if(selectedCan!=null){
   String cur=id==null?"":id.getText().toString();
   if(cur.isEmpty()&&!canValues.isEmpty())cur=String.format(Locale.ROOT,"0x%X",canValues.get(0));
   selectedCan.setText(cur);
  }
 }
 void openCanList(){
  if(canPopup!=null&&canPopup.isShowing()){canPopup.dismiss();return;}
  LinearLayout holder=new LinearLayout(this);holder.setOrientation(1);holder.setBackgroundColor(Color.WHITE);
  ScrollView sc=new ScrollView(this);sc.addView(holder);
  int dp=(int)getResources().getDisplayMetrics().density;
  for(int x:canValues){
   TextView item=new TextView(this);
   item.setText(String.format(Locale.ROOT,"0x%X%s",x,MQBCrc.customTables().containsKey(x)?" ★":""));
   item.setTextColor(Color.rgb(18,31,44));item.setTextSize(17);item.setPadding(18*dp,13*dp,12*dp,13*dp);
   holder.addView(item,new LinearLayout.LayoutParams(-1,-2));
   item.setOnClickListener(v->{
    id.setText(String.format(Locale.ROOT,"0x%X",x));selectedCan.setText(String.format(Locale.ROOT,"0x%X",x));
    if(canPopup!=null)canPopup.dismiss();
   });
  }
  int width=selectedCan.getWidth();
  canPopup=new PopupWindow(sc,width,Math.min(310*dp,canValues.size()*50*dp),true);
  canPopup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
  canPopup.setOutsideTouchable(true);canPopup.setElevation(8*dp);
  canPopup.setOnDismissListener(()->arrowCan.setText("▼"));
  arrowCan.setText("▲");
  canPopup.showAsDropDown(selectedCan,0,4*dp);
 }
 void theme(int choice){int season=choice;if(season==0){int m=Calendar.getInstance().get(Calendar.MONTH);season=(m==11||m<=1)?1:m<=4?2:m<=7?3:4;}int[][] colors={{0,0},{0xFF0C274F,0xFFE6F5FF},{0xFF478CB0,0xFFE4F5DD},{0xFF3D94BB,0xFFFFE6A8},{0xFF8A543E,0xFFFFD9A5}};GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{colors[season][0],colors[season][1]});frame.setBackground(d); seasonIndex=season; boolean photo=getPreferences(0).getBoolean("photo",true); image.setVisibility(photo?View.VISIBLE:View.GONE); veil.setVisibility(photo?View.VISIBLE:View.GONE);veil.setBackgroundColor(Color.argb(getPreferences(0).getInt("opacity",70)*255/100,0,0,0));if(photo)fetchPhoto(season);}
 public
 static final String[] PHOTOS={
 "",
 "Russia, Krasnoyarsk, Tundra 2022 - SS.jpg",
 "Tundra in Siberia.jpg",
 "Siberian Tundra.jpg",
 "Arctic tundra in autumn colors near lake Lovozero in Murmansk Oblast, Russia.jpg"};
 void fetchPhoto(int n){
  final String file=PHOTOS[n];final int requested=n;
  new Thread(()->{try{
   String url="https://commons.wikimedia.org/wiki/Special:FilePath/"+java.net.URLEncoder.encode(file,"UTF-8").replace("+","%20")+"?width=960";
   java.net.HttpURLConnection c=(java.net.HttpURLConnection)new URL(url).openConnection();
   c.setConnectTimeout(10000);c.setReadTimeout(12000);c.setRequestProperty("User-Agent","VW-MQB-CRC/5.0 (Android app)");
   Bitmap bmp;try(InputStream in=c.getInputStream()){bmp=BitmapFactory.decodeStream(in);}
   if(bmp!=null)runOnUiThread(()->{if(seasonIndex==requested&&getPreferences(0).getBoolean("photo",true))image.setImageBitmap(bmp);});
  }catch(Exception ignored){} }).start();
 }

 void showTableManager(){
  ScrollView scroll=new ScrollView(this);
  LinearLayout content=new LinearLayout(this);
  content.setOrientation(LinearLayout.VERTICAL);
  content.setPadding(28,18,28,16);
  scroll.addView(content);
  TextView info=new TextView(this);
  info.setText("Просмотр, добавление и изменение таблиц CRC по CAN ID.\nВстроенные таблицы сохраняются при экспорте автоматически.");
  info.setTextColor(Color.DKGRAY);
  content.addView(info);
  TextView idLabel=new TextView(this);idLabel.setText("CAN ID (HEX)");content.addView(idLabel);
  EditText managerId=new EditText(this);
  managerId.setSingleLine(true);
  managerId.setText(id.getText().toString());
  styleInput(managerId);
  content.addView(managerId);
  TextView tableLabel=new TextView(this);tableLabel.setText("Таблица CRC — 16 HEX-байтов");content.addView(tableLabel);
  EditText managerTable=new EditText(this);
  managerTable.setMinLines(3);
  styleInput(managerTable);
  content.addView(managerTable);
  TextView status=new TextView(this);status.setTextColor(Color.rgb(30,80,105));content.addView(status);
  final Runnable read=()->{
   try{
    int key=Integer.parseInt(managerId.getText().toString().trim().replaceFirst("(?i)^0x",""),16);
    int[] values=MQBCrc.get(key);
    if(values==null){managerTable.setText("");status.setText("Таблица не найдена. Можно создать новую.");}
    else {managerTable.setText(MQBCrc.tableHex(values));status.setText(MQBCrc.customTables().containsKey(key)?"Пользовательская таблица":"Встроенная таблица");}
   }catch(Exception e){status.setText("Некорректный CAN ID");}
  };
  read.run();
  Button readButton=new Button(this);readButton.setText("ПОКАЗАТЬ ТАБЛИЦУ");content.addView(readButton);readButton.setOnClickListener(v->read.run());
  Button saveButton=new Button(this);saveButton.setText("СОХРАНИТЬ / ДОБАВИТЬ");content.addView(saveButton);
  saveButton.setOnClickListener(v->{
   try{
    int key=Integer.parseInt(managerId.getText().toString().trim().replaceFirst("(?i)^0x",""),16);
    MQBCrc.put(key,MQBCrc.parseTable(managerTable.getText().toString()));
    save();refresh();status.setText("Таблица 0x"+Integer.toHexString(key).toUpperCase(Locale.ROOT)+" сохранена.");
   }catch(Exception e){status.setText("Ошибка: "+e.getMessage());}
  });
  Button removeButton=new Button(this);removeButton.setText("УДАЛИТЬ ПОЛЬЗОВАТЕЛЬСКУЮ ТАБЛИЦУ");content.addView(removeButton);
  removeButton.setOnClickListener(v->{
   try{
    int key=Integer.parseInt(managerId.getText().toString().trim().replaceFirst("(?i)^0x",""),16);
    MQBCrc.remove(key);save();refresh();read.run();
    status.setText("Пользовательская запись удалена. Для встроенных ID восстановлена исходная таблица.");
   }catch(Exception e){status.setText("Ошибка: "+e.getMessage());}
  });
  Button export=new Button(this);export.setText("ЭКСПОРТ ВСЕХ ТАБЛИЦ");content.addView(export);
  export.setOnClickListener(v->{
   Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
   i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/plain");
   i.putExtra(Intent.EXTRA_TITLE,"VW_MQB_CRC_tables.txt");startActivityForResult(i,104);
  });
  Button imp=new Button(this);imp.setText("ИМПОРТ ТАБЛИЦ");content.addView(imp);
  imp.setOnClickListener(v->{
   Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
   i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/plain");startActivityForResult(i,105);
  });
  new AlertDialog.Builder(this).setTitle("Управление таблицами CRC")
   .setView(scroll).setPositiveButton("Готово",null).show();
 }

 void showSettings(){
  ScrollView sc=new ScrollView(this);LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(30,20,30,15);sc.addView(content);
  TextView modeLabel=new TextView(this);modeLabel.setText("Фон");modeLabel.setTextColor(Color.BLACK);content.addView(modeLabel);
  Spinner mode=new Spinner(this);mode.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Фотография","Градиент"}));mode.setSelection(getPreferences(0).getBoolean("photo",true)?0:1);content.addView(mode);
  TextView seasonLabel=new TextView(this);seasonLabel.setText("Время года");seasonLabel.setTextColor(Color.BLACK);content.addView(seasonLabel);
  Spinner ss=new Spinner(this);ss.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Автоматически","Зима","Весна","Лето","Осень"}));ss.setSelection(getPreferences(0).getInt("season",0));content.addView(ss);
  TextView opacityLabel=new TextView(this);opacityLabel.setText("Затемнение фотографии: "+getPreferences(0).getInt("opacity",70)+"%");content.addView(opacityLabel);
  SeekBar opacity=new SeekBar(this);opacity.setMax(100);opacity.setProgress(getPreferences(0).getInt("opacity",70));content.addView(opacity);
  opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}public void onProgressChanged(SeekBar b,int p,boolean user){opacityLabel.setText("Затемнение фотографии: "+p+"%");}});
  Button about=new Button(this);about.setText("О программе и источники");content.addView(about);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Настройки оформления").setView(sc).setNegativeButton("Отмена",null).setPositiveButton("Сохранить",(d,w)->{getPreferences(0).edit().putBoolean("photo",mode.getSelectedItemPosition()==0).putInt("season",ss.getSelectedItemPosition()).putInt("opacity",opacity.getProgress()).apply();theme(ss.getSelectedItemPosition());}).create();
  about.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("О программе • VW MQB CRC v5").setMessage(
 "Калькулятор CRC для CAN-сообщений Volkswagen MQB.\\n\\n"+
 "Основа идеи и таблиц: commaai/opendbc, ревизия 962c0c18e8b6638e62d786d6bebb08846b177ef4\\nhttps://github.com/commaai/opendbc/tree/962c0c18e8b6638e62d786d6bebb08846b177ef4\\n\\n"+
 "Код приложения сгенерирован с помощью нейросети OpenAI ChatGPT и требует независимой проверки перед применением к реальным системам автомобиля.\\n\\n"+
 "Фотографии: Wikimedia Commons (сохраняются права авторов):\\n"+
 "Зима — Svetlana Sibiryakova, Russia, Krasnoyarsk, Tundra 2022 - SS.jpg, CC BY-SA 4.0\\n"+
 "Весна — Dr. Andreas Hugentobler, Tundra in Siberia.jpg, CC BY 2.0 DE (условная сезонная иллюстрация)\\n"+
 "Лето — Ninaras, Siberian Tundra.jpg, CC BY 4.0\\n"+
 "Осень — Artem Abdukakharov, Arctic tundra in autumn colors near lake Lovozero in Murmansk Oblast, Russia.jpg, CC BY 4.0.\\n"+
 "Страницы лицензий и авторов: https://commons.wikimedia.org/wiki/File: (названия файлов выше). Лицензии: https://creativecommons.org/licenses/by/4.0/ ; https://creativecommons.org/licenses/by-sa/4.0/ ; https://creativecommons.org/licenses/by/2.0/de/ .\\n"+
 "Снимки могут быть обрезаны по размеру экрана, сами изображения не изменены."
 ).setPositiveButton("OK",null).show());
  dialog.show();
 }
 public void onCreate(Bundle b){super.onCreate(b);load();frame=new FrameLayout(this);image=new ImageView(this);image.setScaleType(ImageView.ScaleType.CENTER_CROP);frame.addView(image,new FrameLayout.LayoutParams(-1,-1));veil=new View(this);frame.addView(veil,new FrameLayout.LayoutParams(-1,-1));ScrollView scroll=new ScrollView(this);page=new LinearLayout(this);page.setOrientation(1);page.setPadding(22,25,22,35);scroll.addView(page);frame.addView(scroll,new FrameLayout.LayoutParams(-1,-1));setContentView(frame);
 LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.HORIZONTAL);header.setGravity(Gravity.CENTER_VERTICAL);
 TextView symbol=new TextView(this);symbol.setText("MQB");symbol.setTextSize(26);symbol.setTypeface(null,android.graphics.Typeface.BOLD);
 symbol.setTextColor(Color.WHITE);symbol.setShadowLayer(5,0,2,Color.rgb(15,89,141));
 header.addView(symbol);
 TextView crctitle=new TextView(this);crctitle.setText(" CRC");crctitle.setTextSize(27);
 crctitle.setTypeface(null,android.graphics.Typeface.BOLD);crctitle.setTextColor(Color.rgb(47,195,255));
 header.addView(crctitle);page.addView(header);
 button("⚙ Настройки / О программе",()->showSettings()); theme(getPreferences(0).getInt("season",0));
 label("Список CAN ID");
 FrameLayout selectFrame=new FrameLayout(this);
 selectedCan=new TextView(this);selectedCan.setTextColor(Color.rgb(18,31,44));selectedCan.setTextSize(17);
 selectedCan.setGravity(Gravity.CENTER_VERTICAL);selectedCan.setPadding(18,15,58,15);
 selectedCan.setBackground(translucentField());
 selectFrame.addView(selectedCan,new FrameLayout.LayoutParams(-1,-2));
 arrowCan=new TextView(this);arrowCan.setText("▼");arrowCan.setTextSize(18);
 arrowCan.setTextColor(Color.rgb(18,31,44));arrowCan.setGravity(Gravity.CENTER);
 int dp=(int)getResources().getDisplayMetrics().density;
 FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(48*dp,-1,Gravity.RIGHT);
 selectFrame.addView(arrowCan,ap);
 selectFrame.setOnClickListener(v->openCanList());
 selectedCan.setOnClickListener(v->openCanList());
 arrowCan.setOnClickListener(v->openCanList());
 page.addView(selectFrame);
 label("CAN ID (HEX)");id=new EditText(this);id.setSingleLine(true);id.setText("0x122");styleInput(id);page.addView(id);
 label("CAN данные (2–8 байт HEX)");data=new EditText(this);data.setSingleLine(true);data.setText("00 01 02 03 04 05 06 07");styleInput(data);page.addView(data);
 result=new TextView(this);result.setTextSize(18);result.setTextColor(Color.WHITE);result.setTextIsSelectable(true);
 button("РАССЧИТАТЬ CRC",()->{byte[] p=MQBCrc.parseHex(data.getText().toString());int crc=MQBCrc.calculate(currentId(),p);p[0]=(byte)crc;StringBuilder s=new StringBuilder(String.format(Locale.ROOT,"CRC: %02X\nCAN кадр: ",crc));for(byte x:p)s.append(String.format(Locale.ROOT,"%02X ",x&255));result.setText(s.toString());});page.addView(result);
 button("☷ Управление таблицами CRC",()->showTableManager());
 refresh();selectionReady=true;
 }
 String exportText(){StringBuilder s=new StringBuilder("VW_MQB_CRC_TABLES_V1\n");{TreeMap<Integer,int[]> all=new TreeMap<>(MQBCrc.builtinTables());all.putAll(MQBCrc.customTables());for(Map.Entry<Integer,int[]> e:all.entrySet())s.append(String.format(Locale.ROOT,"0x%X ",e.getKey())).append(MQBCrc.tableHex(e.getValue())).append('\n');}return s.toString();}
 Map<Integer,int[]> parseImport(String text){String[] lines=text.split("\\r?\\n");if(lines.length==0||!lines[0].trim().equals("VW_MQB_CRC_TABLES_V1"))throw new IllegalArgumentException("Неверный формат");Map<Integer,int[]> out=new HashMap<>();for(int i=1;i<lines.length;i++){String l=lines[i].trim();if(l.isEmpty())continue;String[] parts=l.split("\\s+",2);if(parts.length!=2)throw new IllegalArgumentException("Строка "+(i+1));long key=Long.parseLong(parts[0].replaceFirst("(?i)^0x",""),16);if(key<0||key>0x1FFFFFFFL)throw new IllegalArgumentException("CAN ID");out.put((int)key,MQBCrc.parseTable(parts[1]));}return out;}
 @Override protected void onActivityResult(int req,int status,Intent intent){super.onActivityResult(req,status,intent);if(status!=RESULT_OK||intent==null||intent.getData()==null)return;try{Uri u=intent.getData();if(req==104){try(OutputStream out=getContentResolver().openOutputStream(u)){out.write(exportText().getBytes(StandardCharsets.UTF_8));}result.setText("Экспорт выполнен");}if(req==105){ByteArrayOutputStream buf=new ByteArrayOutputStream();try(InputStream in=getContentResolver().openInputStream(u)){byte[] x=new byte[4096];int n;while((n=in.read(x))!=-1){if(buf.size()+n>65536)throw new IOException("Слишком большой файл");buf.write(x,0,n);}}Map<Integer,int[]> incoming=parseImport(new String(buf.toByteArray(),StandardCharsets.UTF_8));new AlertDialog.Builder(this).setTitle("Импорт таблиц").setMessage("Импортировать "+incoming.size()+" таблиц? Совпадающие ID будут заменены.").setNegativeButton("Отмена",null).setPositiveButton("Импорт",(d,w)->{for(Map.Entry<Integer,int[]> e:incoming.entrySet())MQBCrc.put(e.getKey(),e.getValue());save();refresh();result.setText("Импортировано: "+incoming.size());}).show();}}catch(Exception e){result.setText("Ошибка: "+e.getMessage());}}
}