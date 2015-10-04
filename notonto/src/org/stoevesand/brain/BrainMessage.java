package org.stoevesand.brain;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class BrainMessage {

	String _errorText = null;

	public String getErrorText() {
		return _errorText;
	}

	public void setErrorText(String errorText) {
		this._errorText = errorText;
	}

	String _prefixErrorText = null;

	public String getPrefixErrorText() {
		return _prefixErrorText;
	}

	public void setPrefixErrorText(String errorText) {
		this._prefixErrorText = errorText;
	}

	String _pwErrorText = null;

	public String getPwErrorText() {
		return _pwErrorText;
	}

	public void setPwErrorText(String errorText) {
		this._pwErrorText = errorText;
	}

	String _intervallsDateError = null;

	public String getIntervallsDateError() {
		return _intervallsDateError;
	}

	public void setIntervallsDateError(String intervallsDateError) {
		_intervallsDateError = intervallsDateError;
	}
	
}
