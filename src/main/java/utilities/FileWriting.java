package utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public final class FileWriting implements Closeable
{
    private final BufferedWriter writer;
    private int space;

    public FileWriting(String path) throws IOException
    {
        this(new File(path));
    }

    public FileWriting(File file) throws IOException
    {
        this(new FileWriter(file));
    }

    public FileWriting(Writer writer)
    {
        this(new BufferedWriter(writer));
    }

    public FileWriting(BufferedWriter writer)
    {
        this.writer = writer;
    }

    public void write(JSONArray array) throws IOException
    {
        writer.write("[");
        writer.newLine();

        this.space+=2;
        String space = spaceBuilder();

        for(int i = 0; i < array.length(); i++)
        {
            Object object = array.get(i);

            if(object instanceof Number || object instanceof Boolean) writer.write(space+object);
            else if(object instanceof JSONObject) write((JSONObject) object, true);
            else if(object instanceof JSONArray) write((JSONArray) object);
            else writer.write(space+"\""+object.toString()+"\"");

            if(i < array.length()-1) writer.write(",");
            writer.newLine();
        }

        this.space-=2;
        space = spaceBuilder();

        writer.write(space+"]");
    }

    private void write(JSONObject jsonObject, boolean spacing) throws IOException
    {
        writer.write((spacing ? spaceBuilder() : "") + "{");
        writer.newLine();

        this.space+=2;
        String space = spaceBuilder();

        int i = 0;
        final int max = jsonObject.length();

        for(String key : jsonObject.keySet())
        {
            writer.write(space+"\""+key+"\":");
            Object object = jsonObject.get(key);

            if(object instanceof Number || object instanceof Boolean) writer.write(object.toString());
            else if(object instanceof JSONObject) write((JSONObject) object, false);
            else if(object instanceof JSONArray) write((JSONArray) object);
            else writer.write("\""+object.toString()+"\"");

            if(i < max-1) writer.write(",");
            i++;

            writer.newLine();
        }

        this.space-=2;
        space = spaceBuilder();

        writer.write(space+"}");
    }

    public void write(JSONObject jsonObject) throws IOException
    {
        write(jsonObject, false);
    }

    private String spaceBuilder()
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < space; i++) builder.append(" ");
        return builder.length() == 0 ? "" : builder.toString();
    }

    public void flush() throws IOException
    {
        writer.flush();
    }

    public void close() throws IOException
    {
        writer.close();
    }
}
