package utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileReading
{
    private final String json;

    /**
     * Creates a new File using the specified <code>path</code>.
     * 
     * @param path the path of the given file, of type <code>String</code>
     * @throws IOException
     */
    public FileReading(String path) throws IOException
    {
        this(new File(path));
    }

    /**
     * 
     * @param file
     * @throws IOException
     */
    public FileReading(File file) throws IOException
    {
        this(new InputStreamReader(new FileInputStream(file)));
    }

    public FileReading(Reader reader) throws IOException
    {
        this(new BufferedReader(reader));
    }

    public FileReading(BufferedReader reader) throws IOException
    {
        json = load(reader);
    }

    private String load(BufferedReader reader) throws IOException
    {
        StringBuilder builder = new StringBuilder();

        while(reader.ready()) builder.append(reader.readLine());

        reader.close();

        return builder.length() == 0 ? "[]" : builder.toString();
    }

    public static <E> List<E> toList(String path)
    {
        return toList(new File(path));
    }

    public static <E> List<E> toList(File file)
    {
        try
        {
            return toList(new InputStreamReader(new FileInputStream(file)));
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>();
    }

    public static <E> List<E> toList(Reader reader)
    {
        return toList(new BufferedReader(reader));
    }

    @SuppressWarnings("unchecked")
	public static <E> List<E> toList(BufferedReader bufferedReader)
    {
        List<E> list= new ArrayList<>();

        try
        {
            FileReading reader = new FileReading(bufferedReader);
            JSONArray array = reader.toJSONArray();
            for(int i = 0; i < array.length(); i++)
            {
                try
                {
                    list.add((E) array.get(i));
                }catch(ClassCastException e){}
            }
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }

        return list;
    }

    public static <V> Map<String, V> toMap(String path)
    {
        return toMap(new File(path));
    }

    public static <V> Map<String, V> toMap(File file)
    {
        try
        {
            return toMap(new InputStreamReader(new FileInputStream(file)));
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
        return new HashMap<>();
    }

    public static <V> Map<String, V> toMap(Reader reader)
    {
        return toMap(new BufferedReader(reader));
    }

    @SuppressWarnings("unchecked")
	public static <V> Map<String, V> toMap(BufferedReader bufferedReader)
    {
        Map<String, V> map = new HashMap<>();

        try
        {
            FileReading reader = new FileReading(bufferedReader);
            JSONObject object = reader.toJSONObject();
            for(String key : object.keySet())
            {
                @SuppressWarnings("unused")
				Object obj = object.get(key);
                try
                {
                    map.put(key, (V) object.get(key));
                }
                catch(ClassCastException e) {}
            }
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }

        return map;
    }

    public JSONArray toJSONArray()
    {
        return new JSONArray(json);
    }

    public JSONObject toJSONObject()
    {
        return new JSONObject(json);
    }
}

