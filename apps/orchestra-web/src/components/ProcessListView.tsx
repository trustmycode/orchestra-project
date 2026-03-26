import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ProcessModel, ProtocolSpecSummary } from '../types';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Button } from './ui/button';
import { Sparkles } from 'lucide-react';
import AiWizard from './ai/AiWizard';
import { useTranslation } from 'react-i18next';

interface Props {
  processes: ProcessModel[];
  specs: ProtocolSpecSummary[];
  onSelectProcess: (id: string) => void;
}

const ProcessListView: React.FC<Props> = ({ processes, specs, onSelectProcess }) => {
  const [isWizardOpen, setIsWizardOpen] = useState(false);
  const navigate = useNavigate();
  const { t } = useTranslation();

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-2xl font-bold tracking-tight">{t('lists.processesTitle')}</h2>
        <Button variant="ai" onClick={() => setIsWizardOpen(true)}>
          <Sparkles className="mr-2 h-4 w-4" /> {t('lists.generateSuite')}
        </Button>
      </div>
      {processes.length === 0 ? (
        <p>{t('lists.noData')}</p>
      ) : (
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>{t('common.name')}</TableHead>
                <TableHead>{t('lists.sourceType')}</TableHead>
                <TableHead>{t('common.createdAt')}</TableHead>
                <TableHead className="text-right">{t('common.actions')}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {processes.map((process) => (
                <TableRow key={process.id}>
                  <TableCell className="font-mono text-xs" title={process.id}>
                    {process.id.substring(0, 8)}...
                  </TableCell>
                  <TableCell className="font-medium">{process.name}</TableCell>
                  <TableCell>{process.sourceType}</TableCell>
                  <TableCell>{new Date(process.createdAt).toLocaleString()}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="outline" size="sm" onClick={() => onSelectProcess(process.id)}>
                      {t('lists.viewDiagram')}
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <AiWizard
        isOpen={isWizardOpen}
        onClose={() => setIsWizardOpen(false)}
        processes={processes}
        specs={specs}
        target="SUITE"
        onSuccess={(suiteId) => navigate(`/suites/${suiteId}`)}
      />
    </div>
  );
};

export default ProcessListView;
