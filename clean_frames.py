import os, re

for root, _, files in os.walk('src/main/java/org/example/frame'):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as file:
                content = file.read()
            
            # Remove private implementations
            content = re.sub(r'private JPanel statCard.*?\s*return card;\s*}', '', content, flags=re.DOTALL)
            content = re.sub(r'private Component infoLine.*?\s*return lbl;\s*}', '', content, flags=re.DOTALL)
            content = re.sub(r'private JPanel buildPosterPanel.*?\s*};\s*}', '', content, flags=re.DOTALL)
            
            # Repoint statCard
            content = re.sub(r'(?<!WidgetFactory\.)statCard\(', 'WidgetFactory.createStatCard(', content)
            
            # Repoint infoLine
            content = re.sub(r'(?<!WidgetFactory\.)infoLine\(', 'WidgetFactory.createInfoLine(', content)

            # Repoint buildPosterPanel
            content = re.sub(r'(?<!WidgetFactory\.)buildPosterPanel\(.*?,\s*(\d+),\s*(\d+)\)', r'WidgetFactory.createPosterPanel(\g<0>)', content)
            
            # Let's cleanly replace the buildPosterPanel calls. Above regex was a bit tricky with g<0>.
            with open(path, 'w', encoding='utf-8') as file:
                file.write(content)
