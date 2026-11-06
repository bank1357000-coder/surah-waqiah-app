import re, os, glob, collections
root = '/app/android/WaqiahQuran/app/src'
files = glob.glob(root + '/**/*.kt', recursive=True)
decl = collections.defaultdict(set)  # package -> names
info = {}
for f in files:
    src = open(f, encoding='utf-8').read()
    pkg = re.search(r'^package\s+([\w.]+)', src, re.M).group(1)
    names = set(re.findall(r'^(?:@\S+\s+)*(?:(?:private|internal|public|data|sealed|abstract|enum|inline|annotation|value|open)\s+)*(?:class|interface|object|fun|val|typealias)\s+(?:<[^>]*>\s*)?(?:[\w.]+\.)?(\w+)', src, re.M))
    decl[pkg] |= names
    info[f] = (pkg, src)
builtin = set('''String Int Long Float Double Boolean Unit Any Nothing List Map Set Array MutableList MutableMap Pair Triple
Char Byte Short Enum Exception IllegalStateException IllegalArgumentException Throwable Comparable Sequence IntRange Result
Charsets System Math StringBuilder Regex Collection Iterable Suppress OptIn Volatile JvmStatic JvmInline Deprecated Retention
AnnotationRetention Target R BuildConfig TODO Test Before After Assert RunWith File CharSequence Lazy Number Override Synchronized'''.split())
for f, (pkg, src) in sorted(info.items()):
    imports = set(m.split('.')[-1] for m in re.findall(r'^import\s+([\w.]+)', src, re.M))
    aliases = set(re.findall(r'^import\s+[\w.]+\s+as\s+(\w+)', src, re.M))
    body = re.sub(r'"(?:\\.|[^"\\])*"', '""', src)
    body = re.sub(r'//.*', '', body)
    body = re.sub(r'/\*.*?\*/', '', body, flags=re.S)
    body = '\n'.join(l for l in body.split('\n') if not l.startswith(('import', 'package')))
    local = set(re.findall(r'(?:class|interface|object|fun|val|var|typealias|enum class)\s+(?:<[^>]*>\s*)?(?:[\w.]+\.)?(\w+)', body))
    local |= set(re.findall(r'<\s*(?:reified\s+)?(\w+)\s*(?::|,|>)', body))  # generics
    used = set(re.findall(r'(?<![\w.])([A-Z]\w*)', body))
    missing = used - imports - aliases - decl[pkg] - local - builtin
    missing = {m for m in missing if not re.fullmatch(r'[A-Z0-9_]+', m)}
    if missing:
        print(os.path.relpath(f, root), sorted(missing))
print("=== lowercase checks")
kw = set('if when while for return catch try throw super this listOf setOf mapOf emptyList emptyMap emptySet mutableListOf mutableMapOf mutableSetOf println require check checkNotNull requireNotNull error repeat run with apply also let takeIf lazy arrayOf emptyArray maxOf minOf buildString buildList until downTo step to in is as object fun val var by remember get set invoke assertEquals assertTrue assertFalse assertNotNull assertNull fail intArrayOf require arrayListOf'.split())
ext = set('padding fillMaxWidth fillMaxSize fillMaxHeight size height width testTag clickable background clip border verticalScroll horizontalScroll alpha semantics offset drawBehind drawWithContent wrapContentSize statusBarsPadding navigationBarsPadding systemBarsPadding windowInsetsPadding aspectRatio rotate scale shadow graphicsLayer toggleable selectable selectableGroup heightIn widthIn defaultMinSize sizeIn collectAsStateWithLifecycle stateIn map combine flatMapLatest distinctUntilChanged filterNotNull onStart catch update asStateFlow first withTransaction toRoute edit preferencesDataStoreFile collectAsState animateItem nestedScroll imePadding requiredSize zIndex combinedClicked combinedClickable focusable sp dp em copy launch emitAll flowOn onEach mapLatest stateIn shareIn sortedBy'.split())
for f, (pkg, src) in sorted(info.items()):
    imports = set(m.split('.')[-1] for m in re.findall(r'^import\s+([\w.]+)', src, re.M))
    body = re.sub(r'"(?:\\.|[^"\\])*"', '""', src); body = re.sub(r'//.*', '', body); body = re.sub(r'/\*.*?\*/', '', body, flags=re.S)
    body = '\n'.join(l for l in body.split('\n') if not l.startswith(('import', 'package')))
    local = set(re.findall(r'fun\s+(?:<[^>]*>\s*)?(?:[\w.<>?, ]+\.)?(\w+)\s*\(', body)) | set(re.findall(r'(?:val|var)\s+(\w+)', body)) | set(re.findall(r'(\w+)\s*:', body))
    allfuns = set().union(*decl.values())
    calls = set(re.findall(r'(?<![\w.])([a-z]\w*)\s*[({]', body))
    miss = calls - imports - local - kw - decl[pkg]
    dots = set(re.findall(r'\.\s*([a-z]\w*)\b', body)) & ext
    dmiss = {d for d in dots if d not in imports and d not in local and d not in ('copy','launch','update','first','map','sp','dp','em') }
    # specific: .sp/.dp need imports
    for u in ('sp','dp','em'):
        if re.search(r'\d\.%s\b' % u, body) and u not in imports: dmiss.add(u)
    if miss or dmiss: print(os.path.relpath(f, root), 'calls:', sorted(miss), 'ext:', sorted(dmiss))
