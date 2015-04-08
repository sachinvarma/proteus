package com.flipkart.layoutengine.view;

import android.util.Log;
import android.view.View;

import com.flipkart.layoutengine.ParserContext;
import com.flipkart.layoutengine.binding.Binding;
import com.flipkart.layoutengine.provider.DataParsingAdapter;
import com.flipkart.layoutengine.provider.GsonProvider;
import com.flipkart.layoutengine.provider.Provider;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Map;

/**
 * A {@link ProteusView} implementation to update the data
 * associated with a {@link android.view.View} built using a {@link com.flipkart.layoutengine.builder.LayoutBuilder}.
 *
 * @author Aditya Sharat {@literal <aditya.sharat@flipkart.com>}
 */
public class SimpleProteusView implements ProteusView {

    private static final Character PREFIX = DataParsingAdapter.PREFIX;
    private static final String TAG = SimpleProteusView.class.getSimpleName();

    private Map<String, Binding> mapOfBindings;
    private View view;
    private Gson gson = new Gson();

    public SimpleProteusView(View view, Map<String, Binding> mapOfBindings) {
        this.view = view;
        this.mapOfBindings = mapOfBindings;
    }

    @Override
    public View getView() {
        return this.view;
    }

    @Override
    public Map<String, Binding> getBindings() {
        return this.mapOfBindings;
    }

    @Override
    public View updateView(JsonObject data) {

        for (Map.Entry<String, Binding> bindingEntry : this.mapOfBindings.entrySet()) {
            this.handleBinding(bindingEntry, data);
        }
        return this.getView();
    }

    private void handleBinding(Map.Entry<String, Binding> bindingEntry, JsonObject data) {
        JsonObject temp = new JsonObject();
        temp.addProperty("value", bindingEntry.getKey());

        JsonElement dataAttribute = temp.get("value");
        Binding binding = bindingEntry.getValue();
        ParserContext context = binding.getParserContext();
        int index = binding.getIndex();

        context.setDataProvider(new GsonProvider(data));

        JsonElement dataValue = getElementFromData(dataAttribute, context.getDataProvider(), index);

        try {
            binding.getParserContext().getLayoutBuilder().handleAttribute(
                    binding.getLayoutHandler(),
                    context,
                    binding.getAttributeKey(),
                    null,
                    dataValue,
                    binding.getView(),
                    binding.getParentView(),
                    index);
        } catch (Exception e) {
            Log.d("data", binding.getAttributeKey() + " " +
                    dataValue.toString() + " ");
        }
    }

    public static JsonElement getElementFromData(JsonElement element, Provider dataProvider, int childIndex) {
        if (element.isJsonPrimitive()) {
            String dataSourceKey = element.getAsString();
            if (dataSourceKey.length() > 0 && dataSourceKey.charAt(0) == PREFIX) {
                JsonElement tempElement = dataProvider.getObject(dataSourceKey.substring(1), childIndex);
                if (tempElement != null) {
                    element = tempElement;
                } else {
                    Log.e(TAG, "Got null element for " + dataSourceKey);
                }
            }
        }
        return element;
    }
}
