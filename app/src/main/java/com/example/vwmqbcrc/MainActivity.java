package com.example.vwmqbcrc;
import android.app.*;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class MainActivity extends Activity {
 EditText id,data,table; TextView result; Spinner ids,seasons; LinearLayout page; boolean selectionReady=false;
 int currentId(){return Integer.parseInt(id.getText().toString().trim().replaceFirst("(?i)^0x",""),16);}
 void label(String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(16);t.setPadding(0,20,0,3);page.addView(t);}
 Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);page.addView(b);b.setOnClickListener(v->{try{r.run();}catch(Exception e){result.setText("Ошибка: "+e.getMessage());}});return b;}
 void save(){StringBuilder b=new StringBuilder();for(Map.Entry<Integer,int[]> e:MQBCrc.customTables().entrySet())b.append(Integer.toHexString(e.getKey())).append('=').append(MQBCrc.tableHex(e.getValue())).append('\n');getPreferences(0).edit().putString("tables",b.toString()).apply();}
 void load(){MQBCrc.clearCustom();for(String l:getPreferences(0).getString("tables","").split("\\n")){String[] p=l.split("=",2);if(p.length==2)try{MQBCrc.put(Integer.parseInt(p[0],16),MQBCrc.parseTable(p[1]));}catch(Exception ignored){}}}
 void refresh(){TreeSet<Integer> all=new TreeSet<>(MQBCrc.builtinTables().keySet());all.addAll(MQBCrc.customTables().keySet());ArrayList<Integer> values=new ArrayList<>(all);ArrayList<String> names=new ArrayList<>();for(int x:values)names.add(String.format(Locale.ROOT,"0x%X%s",x,MQBCrc.customTables().containsKey(x)?" ★":""));ids.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,names));ids.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long n){if(!selectionReady||pos>=values.size())return;int x=values.get(pos);id.setText(String.format(Locale.ROOT,"0x%X",x));table.setText(MQBCrc.tableHex(MQBCrc.get(x)));}});}
 void theme(int choice){int season=choice;if(season==0){int m=Calendar.getInstance().get(Calendar.MONTH);season=(m==11||m<=1)?1:m<=4?2:m<=7?3:4;}int[][] colors={{0,0},{0xFF0C274F,0xFFE6F5FF},{0xFF478CB0,0xFFE4F5DD},{0xFF3D94BB,0xFFFFE6A8},{0xFF8A543E,0xFFFFD9A5}};GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{colors[season][0],colors[season][1]});page.setBackground(d);}
 public void onCreate(Bundle b){super.onCreate(b);load();ScrollView scroll=new ScrollView(this);page=new LinearLayout(this);page.setOrientation(1);page.setPadding(22,25,22,35);scroll.addView(page);setContentView(scroll);
 TextView title=new TextView(this);title.setText("VOLKSWAGEN MQB CRC");title.setTextSize(25);title.setTextColor(Color.WHITE);page.addView(title);
 label("Крайний Север — сезон оформления");seasons=new Spinner(this);seasons.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Автоматически","Зима","Весна","Лето","Осень"}));page.addView(seasons);int chosen=getPreferences(0).getInt("season",0);seasons.setSelection(chosen);seasons.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int i,long n){theme(i);getPreferences(0).edit().putInt("season",i).apply();}});theme(chosen);
 label("Список CAN ID");ids=new Spinner(this);page.addView(ids);
 label("CAN ID (HEX)");id=new EditText(this);id.setSingleLine(true);id.setText("0x122");page.addView(id);
 label("CAN данные (2–8 байт HEX)");data=new EditText(this);data.setSingleLine(true);data.setText("00 01 02 03 04 05 06 07");page.addView(data);
 result=new TextView(this);result.setTextSize(18);result.setTextColor(Color.WHITE);result.setTextIsSelectable(true);
 button("РАССЧИТАТЬ CRC",()->{byte[] p=MQBCrc.parseHex(data.getText().toString());int crc=MQBCrc.calculate(currentId(),p);p[0]=(byte)crc;StringBuilder s=new StringBuilder(String.format(Locale.ROOT,"CRC: %02X\nCAN кадр: ",crc));for(byte x:p)s.append(String.format(Locale.ROOT,"%02X ",x&255));result.setText(s.toString());});page.addView(result);
 label("Таблица CRC (16 HEX-байтов)");table=new EditText(this);table.setMinLines(2);table.setTextSize(14);page.addView(table);
 button("ПОКАЗАТЬ ТАБЛИЦУ",()->{int[] t=MQBCrc.get(currentId());if(t==null)throw new IllegalArgumentException("Таблица не найдена");table.setText(MQBCrc.tableHex(t));});
 button("СОХРАНИТЬ / ДОБАВИТЬ CAN ID",()->{MQBCrc.put(currentId(),MQBCrc.parseTable(table.getText().toString()));save();refresh();result.setText("Сохранено");});
 button("УДАЛИТЬ ПОЛЬЗОВАТЕЛЬСКУЮ ТАБЛИЦУ",()->{MQBCrc.remove(currentId());save();refresh();result.setText("Удалено");});
 button("ЭКСПОРТ ТАБЛИЦ",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/plain");i.putExtra(Intent.EXTRA_TITLE,"VW_MQB_custom_tables.txt");startActivityForResult(i,104);});
 button("ИМПОРТ ТАБЛИЦ",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/plain");startActivityForResult(i,105);});
 refresh();selectionReady=true;
 }
 String exportText(){StringBuilder s=new StringBuilder("VW_MQB_CRC_TABLES_V1\n");for(Map.Entry<Integer,int[]> e:new TreeMap<>(MQBCrc.customTables()).entrySet())s.append(String.format(Locale.ROOT,"0x%X ",e.getKey())).append(MQBCrc.tableHex(e.getValue())).append('\n');return s.toString();}
 Map<Integer,int[]> parseImport(String text){String[] lines=text.split("\\r?\\n");if(lines.length==0||!lines[0].trim().equals("VW_MQB_CRC_TABLES_V1"))throw new IllegalArgumentException("Неверный формат");Map<Integer,int[]> out=new HashMap<>();for(int i=1;i<lines.length;i++){String l=lines[i].trim();if(l.isEmpty())continue;String[] parts=l.split("\\s+",2);if(parts.length!=2)throw new IllegalArgumentException("Строка "+(i+1));long key=Long.parseLong(parts[0].replaceFirst("(?i)^0x",""),16);if(key<0||key>0x1FFFFFFFL)throw new IllegalArgumentException("CAN ID");out.put((int)key,MQBCrc.parseTable(parts[1]));}return out;}
 @Override protected void onActivityResult(int req,int status,Intent intent){super.onActivityResult(req,status,intent);if(status!=RESULT_OK||intent==null||intent.getData()==null)return;try{Uri u=intent.getData();if(req==104){try(OutputStream out=getContentResolver().openOutputStream(u)){out.write(exportText().getBytes(StandardCharsets.UTF_8));}result.setText("Экспорт выполнен");}if(req==105){ByteArrayOutputStream buf=new ByteArrayOutputStream();try(InputStream in=getContentResolver().openInputStream(u)){byte[] x=new byte[4096];int n;while((n=in.read(x))!=-1){if(buf.size()+n>65536)throw new IOException("Слишком большой файл");buf.write(x,0,n);}}Map<Integer,int[]> incoming=parseImport(new String(buf.toByteArray(),StandardCharsets.UTF_8));new AlertDialog.Builder(this).setTitle("Импорт таблиц").setMessage("Импортировать "+incoming.size()+" таблиц? Совпадающие ID будут заменены.").setNegativeButton("Отмена",null).setPositiveButton("Импорт",(d,w)->{for(Map.Entry<Integer,int[]> e:incoming.entrySet())MQBCrc.put(e.getKey(),e.getValue());save();refresh();result.setText("Импортировано: "+incoming.size());}).show();}}catch(Exception e){result.setText("Ошибка: "+e.getMessage());}}
}