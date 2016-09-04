package pl.otros.logview.filter;

import pl.otros.logview.api.model.LogData;

/**
 * Created by cc on 9/3/2016.
 */
public class UserFilter extends MultipleSelectionFilter {
    public UserFilter() {
        super("User", "User", "User:", 'u');
    }

    @Override
    String getFilteredString(LogData logData) {
        return logData.getUser();
    }
}
