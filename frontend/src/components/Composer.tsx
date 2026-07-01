import { useRef, type KeyboardEvent } from 'react';

type ComposerProps = {
  prompt: string;
  files?: File[];
  disabled?: boolean;
  placeholder: string;
  submitLabel: string;
  accept?: string;
  allowFiles?: boolean;
  onPromptChange: (value: string) => void;
  onFilesChange?: (files: File[]) => void;
  onSubmit: () => void;
};

export function Composer({
  prompt,
  files = [],
  disabled = false,
  placeholder,
  submitLabel,
  accept,
  allowFiles = false,
  onPromptChange,
  onFilesChange,
  onSubmit,
}: ComposerProps) {
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      onSubmit();
    }
  }

  return (
    <div className="composer-shell">
      {allowFiles && files.length > 0 ? (
        <div className="upload-strip">
          {files.map((file) => (
            <div key={`${file.name}-${file.lastModified}`} className="upload-chip">
              <span>{file.name}</span>
              <button
                type="button"
                onClick={() => onFilesChange?.(files.filter((item) => item !== file))}
              >
                Remove
              </button>
            </div>
          ))}
        </div>
      ) : null}

      <div className={`composer-row${allowFiles ? ' with-files' : ' text-only'}`}>
        {allowFiles ? (
          <>
            <input
              ref={fileInputRef}
              hidden
              type="file"
              accept={accept}
              multiple
              onChange={(event) => {
                onFilesChange?.(Array.from(event.target.files ?? []));
                event.target.value = '';
              }}
            />
            <button
              type="button"
              className="icon-button"
              disabled={disabled}
              onClick={() => fileInputRef.current?.click()}
            >
              Attach
            </button>
          </>
        ) : null}

        <textarea
          value={prompt}
          onChange={(event) => onPromptChange(event.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          disabled={disabled}
          rows={1}
        />

        <button type="button" className="send-button" disabled={disabled} onClick={onSubmit}>
          {submitLabel}
        </button>
      </div>
    </div>
  );
}
