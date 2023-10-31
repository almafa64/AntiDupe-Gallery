package com.cyberegylet.antiDupeGallery;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Data
{
	public final int id;
	public final Uri uri;
	public final String folderName;
	public final int fileCount;

	Data(Uri uri, int id, int fileCount, String folderName)
	{
		this.uri = uri;
		this.id = id;
		this.fileCount = fileCount;
		this.folderName = folderName;
	}
}

class MyAdapter3 extends RecyclerView.Adapter<MyAdapter3.ViewHolder>
{
	private final List<Data> datas;

	private final FileManager fileManager;

	public MyAdapter3(List<Data> datas, FileManager fileManager)
	{
		this.datas = datas;
		this.fileManager = fileManager;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public ImageView img;
		public TextView name;
		public TextView count;

		public ViewHolder(View itemView)
		{
			super(itemView);
			img = itemView.findViewById(R.id.image);
			name = itemView.findViewById(R.id.folderName);
			count = itemView.findViewById(R.id.fileCount);
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View contactView = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder, parent, false);
		return new ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull MyAdapter3.ViewHolder holder, int position)
	{
		Data data = datas.get(position);
		fileManager.thumbnailIntoImageView(holder.img, data.uri);
		holder.name.setText(data.folderName);
		holder.count.setText(String.valueOf(data.fileCount));
	}

	@Override
	public int getItemCount()
	{
		return datas.size();
	}
}

public class Main extends Activity
{
	public static void println(String text)
	{
		Log.d("App.Main", text);
	}

	public static void println(Object num) { println(String.valueOf(num)); }

	public static void println() { println(""); }

	private FileManager fileManager;
	private RecyclerView recyclerView;
	ArrayList<Data> datas = new ArrayList<>();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		recyclerView = findViewById(R.id.recycle);
		findViewById(R.id.downBut2).setOnClickListener(v -> recyclerView.scrollToPosition(datas.size() - 1));
		findViewById(R.id.upBut2).setOnClickListener(v -> recyclerView.scrollToPosition(0));

		fileManager = new FileManager(this);
		if (fileManager.hasReadAccess()) fileThings();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if (requestCode == FileManager.STORAGE_REQUEST_CODE && Arrays.stream(grantResults).allMatch(v -> v == 0))
		{
			fileThings();
		}
		else finishAndRemoveTask();
	}

	private void fileThings()
	{
		HashMap<String, Object[]> folderNames = new HashMap<>();

		FileManager.CursorLoopWrapper wrapper = new FileManager.CursorLoopWrapper()
		{
			@Override
			public void run()
			{
				String path = getPath();
				int id = getID();
				//if (path.contains("/.")) return; // check if file is in empty directory
				int lastThing = path.lastIndexOf('/');

				if (lastThing == -1) return; // check if path doesn't have '/' -> some file "can" be in root

				int secondLastThing = path.lastIndexOf('/', lastThing - 1);

				String folderAbs = path.substring(0, lastThing);
				Object[] tmp = folderNames.get(folderAbs);
				if (tmp != null)
				{
					tmp[1] = (Integer) tmp[1] + 1;
					return;
				}

				folderNames.put(folderAbs,
						new Object[]{ fileManager.stringToUri(path), 1, id, path.substring(secondLastThing + 1, lastThing) });
			}
		};
		String sort = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";
		fileManager.allImageAndVideoLoop(sort, wrapper, MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA);

		Comparator<Object[]> comparator = Comparator.comparing((Object[] a) -> ((String) a[3]));
		folderNames.entrySet().stream().sorted(Map.Entry.comparingByValue(comparator)).forEach(m -> {
			Object[] v = m.getValue();
			datas.add(new Data((Uri) v[0], (Integer) v[2], (Integer) v[1], (String) v[3]));
		});
		recyclerView.setAdapter(new MyAdapter3(datas, fileManager));

		findViewById(R.id.load).setVisibility(View.GONE);
		findViewById(R.id.mainLayout).setClickable(false);
	}
}
