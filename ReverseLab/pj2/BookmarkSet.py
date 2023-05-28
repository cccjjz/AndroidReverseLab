# ?description=Set and reset custom bookmarks
# ?shortcut=
# ?deprecated
import datetime
import json
import time
from com.pnfsoftware.jeb.client.api import IScript
from com.pnfsoftware.jeb.core.units import UnitUtil


class BookmarkSet(IScript):
    BMKEY = 'BOOKMARKS'

    def run(self, ctx):
        f = ctx.getFocusedFragment()
        if not f:
            print('Set the focus on a UI fragment, and position the caret at the location you would like to bookmark.')
            return

        label = ctx.getFocusedView().getFragmentLabel(f)
        addr = f.getActiveAddress()
        unit = f.getUnit()
        uid = unit.getUid()
        unitname = unit.getName()
        unitpath = UnitUtil.buildFullyQualifiedUnitPath(unit)

        log('Unit: %d (%s)' % (uid, unitpath))
        log('Address: %s' % addr)
        log('Fragment: %s' % label)

        prj = ctx.getMainProject()
        bmstr = prj.getData(BookmarkSet.BMKEY)
        if bmstr is not None:
            bm = json.loads(bmstr)
        else:
            bm = {}
        labelmap = bm.get(str(uid))
        if labelmap is None:
            labelmap = {}
            bm[uid] = labelmap

        addrmap = labelmap.get(label)
        if addrmap is None:
            addrmap = {}
            labelmap[label] = addrmap

        e = addrmap.get(addr)
        if e:
            log('Found existing entry')
            comment = e[2]
            savedts = e[3]
            title = 'Update a bookmark'
            caption = 'Current comment. (Clear to delete the bookmark.)\nSet on ' + datetime.datetime.fromtimestamp(savedts).ctime()
        else:
            comment = 'bookmarked'
            title = 'Add a bookmark'
            caption = 'Optional comment.'

        comment = ctx.displayQuestionBox(title, caption, comment)
        if comment is None:
            return

        ts = time.time()
        if comment == '':
            log('Removing entry')
            if addr in addrmap:
                del addrmap[addr]
        else:
            log('Adding/modifying entry')
            addrmap[addr] = [unitpath, unitname, comment, ts]

        prj.setData(BookmarkSet.BMKEY, json.dumps(bm), True)


def log(s):
    pass  # print(s)
