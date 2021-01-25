package jadx.gui.ui.codearea;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jadx.api.ICodeInfo;
import jadx.core.utils.StringUtils;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.SearchDialog;
import jadx.gui.utils.NLS;
import jadx.gui.utils.UiUtils;

/**
 * A panel combining a {@link SearchBar and a scollable {@link CodeArea}
 */
public class CodePanel extends JPanel {
	private static final long serialVersionUID = 1117721869391885865L;

	private final SearchBar searchBar;
	private final AbstractCodeArea codeArea;
	private final JScrollPane codeScrollPane;

	public CodePanel(AbstractCodeArea codeArea) {
		this.codeArea = codeArea;
		searchBar = new SearchBar(codeArea);
		codeScrollPane = new JScrollPane(codeArea);

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 0, 0));
		add(searchBar, BorderLayout.NORTH);
		add(codeScrollPane, BorderLayout.CENTER);

		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F, UiUtils.ctrlButton());
		UiUtils.addKeyBinding(codeArea, key, "SearchAction", new AbstractAction() {
			private static final long serialVersionUID = 71338030532869694L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchBar.toggle();
			}
		});
		JMenuItem searchItem = new JMenuItem();
		JMenuItem globalSearchItem = new JMenuItem();
		AbstractAction searchAction = new AbstractAction(NLS.str("popup.search", "")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchBar.toggle();
			}
		};
		AbstractAction globalSearchAction = new AbstractAction(NLS.str("popup.search_global", "")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainWindow mainWindow = codeArea.getContentPanel().getTabbedPane().getMainWindow();
				SearchDialog.searchText(mainWindow, codeArea.getSelectedText());
			}
		};
		searchItem.setAction(searchAction);
		globalSearchItem.setAction(globalSearchAction);
		Separator separator = new Separator();
		JPopupMenu popupMenu = codeArea.getPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				String preferText = codeArea.getSelectedText();
				if (!StringUtils.isEmpty(preferText)) {
					if (preferText.length() >= 23) {
						preferText = preferText.substring(0, 20) + " ...";
					}
					searchAction.putValue(Action.NAME, NLS.str("popup.search", preferText));
					globalSearchAction.putValue(Action.NAME, NLS.str("popup.search_global", preferText));
					popupMenu.add(separator);
					popupMenu.add(globalSearchItem);
					popupMenu.add(searchItem);
				} else {
					popupMenu.remove(separator);
					popupMenu.remove(globalSearchItem);
					popupMenu.remove(searchItem);
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		});

	}

	public void loadSettings() {
		codeArea.loadSettings();
		initLineNumbers();
	}

	public void load() {
		codeArea.load();
		initLineNumbers();
	}

	private void initLineNumbers() {
		LineNumbers numbers = new LineNumbers(codeArea);
		numbers.setUseSourceLines(isUseSourceLines());
		codeScrollPane.setRowHeaderView(numbers);
	}

	private boolean isUseSourceLines() {
		if (codeArea instanceof SmaliArea) {
			return false;
		}
		ICodeInfo codeInfo = codeArea.getNode().getCodeInfo();
		if (codeInfo == null) {
			return false;
		}
		Map<Integer, Integer> lineMapping = codeInfo.getLineMapping();
		if (lineMapping.isEmpty()) {
			return false;
		}
		Set<Integer> uniqueSourceLines = new HashSet<>(lineMapping.values());
		return uniqueSourceLines.size() > 3;
	}

	public SearchBar getSearchBar() {
		return searchBar;
	}

	public AbstractCodeArea getCodeArea() {
		return codeArea;
	}

	public JScrollPane getCodeScrollPane() {
		return codeScrollPane;
	}

	public void refresh() {
		JViewport viewport = getCodeScrollPane().getViewport();
		Point viewPosition = viewport.getViewPosition();
		codeArea.refresh();
		initLineNumbers();
		SwingUtilities.invokeLater(() -> {
			viewport.setViewPosition(viewPosition);
		});
	}
}
